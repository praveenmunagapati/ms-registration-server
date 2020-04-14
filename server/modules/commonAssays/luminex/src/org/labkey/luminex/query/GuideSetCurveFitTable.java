/*
 * Copyright (c) 2014-2019 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.luminex.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.BaseColumnInfo;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.ContainerFilterable;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.VirtualTable;
import org.labkey.api.query.LookupForeignKey;
import org.labkey.api.assay.AssaySchema;

/**
 * User: jeckels
 * Date: Sep 2, 2011
 */
public class GuideSetCurveFitTable extends VirtualTable<LuminexProtocolSchema> implements ContainerFilterable
{
    private final String _curveType;
    private @NotNull ContainerFilter _containerFilter = ContainerFilter.CURRENT;

    /** @param curveType the type of curve to filter the results to. Null means don't filter */
    public GuideSetCurveFitTable(LuminexProtocolSchema schema, ContainerFilter cf, String curveType)
    {
        super(schema.getDbSchema(), LuminexProtocolSchema.GUIDE_SET_CURVE_FIT_TABLE_NAME, schema, cf);
        _curveType = curveType;
        setDescription("Contains one row per curve fit/guide set combination, and contains average and other statistics for all of the matching runs");

        var guideSetIdColumn = new BaseColumnInfo("GuideSetId", this, JdbcType.INTEGER);
        guideSetIdColumn.setLabel("Guide Set");
        guideSetIdColumn.setFk(new LookupForeignKey(cf, "RowId", null)
        {
            @Override
            public TableInfo getLookupTableInfo()
            {
                return _userSchema.createGuideSetTable(getLookupContainerFilter(),false);
            }
        });
        addColumn(guideSetIdColumn);

        var runCountColumn = new BaseColumnInfo("RunCount", this, JdbcType.INTEGER);
        addColumn(runCountColumn);

        var aucAverageColumn = new BaseColumnInfo("AUCAverage", this, JdbcType.REAL);
        aucAverageColumn.setFormat("0.00");
        aucAverageColumn.setLabel("AUC Average");
        aucAverageColumn.setDescription("Average of area under the curve values");
        addColumn(aucAverageColumn);

        var aucStdDevColumn = new BaseColumnInfo("AUCStdDev", this, JdbcType.REAL);
        aucStdDevColumn.setFormat("0.00");
        aucStdDevColumn.setLabel("AUC Std Dev");
        aucStdDevColumn.setDescription("Standard deviation of area under the curve values");
        addColumn(aucStdDevColumn);

        var ec50AverageColumn = new BaseColumnInfo("EC50Average", this, JdbcType.REAL);
        ec50AverageColumn.setFormat("0.00");
        ec50AverageColumn.setLabel("EC50 Average");
        ec50AverageColumn.setDescription("Average of EC50 values");
        addColumn(ec50AverageColumn);

        var ec50StdDevColumn = new BaseColumnInfo("EC50StdDev", this, JdbcType.REAL);
        ec50StdDevColumn.setFormat("0.00");
        ec50StdDevColumn.setLabel("EC50 Std Dev");
        ec50StdDevColumn.setDescription("Standard deviation of EC50 values");
        addColumn(ec50StdDevColumn);

        var curveTypeColumn = new BaseColumnInfo("CurveType", this, JdbcType.VARCHAR);
        addColumn(curveTypeColumn);
    }

    @NotNull
    @Override
    public SQLFragment getFromSQL()
    {
        SQLFragment result = new SQLFragment("SELECT AVG(cf.EC50) AS EC50Average,\n");
        result.append(_userSchema.getDbSchema().getSqlDialect().getStdDevFunction());
        result.append("(cf.EC50) AS EC50StdDev, \n");
        result.append("AVG(cf.AUC) AS AUCAverage, \n");
        result.append("COUNT(DISTINCT at.AnalyteId) AS RunCount, \n");
        result.append(_userSchema.getDbSchema().getSqlDialect().getStdDevFunction());
        result.append("(cf.AUC) AS AUCStdDev, \n");
        result.append("at.GuideSetId,\n");
        result.append("cf.CurveType FROM \n");

        AnalyteTitrationTable analyteTitrationTable = (AnalyteTitrationTable)_userSchema.getTable(LuminexProtocolSchema.ANALYTE_TITRATION_TABLE_NAME, ContainerFilter.EVERYTHING);
        result.append(analyteTitrationTable, "at");
        result.append(", ");

        CurveFitTable curveFitTable = (CurveFitTable)_userSchema.getTable(LuminexProtocolSchema.CURVE_FIT_TABLE_NAME, ContainerFilter.EVERYTHING);
        result.append(curveFitTable, "cf");

        result.append(" WHERE at.AnalyteId = cf.AnalyteId AND at.TitrationId = cf.TitrationId AND at.GuideSetId IS NOT NULL AND at.IncludeInGuideSetCalculation = ?\n");
        result.add(true);
        if (_curveType != null)
        {
            result.append(" AND cf.CurveType = ?");
            result.add(_curveType);
        }
        result.append(" GROUP BY at.GuideSetId, cf.CurveType");

        return result;
    }

    @Override
    public void setContainerFilter(@NotNull ContainerFilter containerFilter)
    {
        checkLocked();
        ContainerFilter.logSetContainerFilter(containerFilter, getClass().getSimpleName(), getName());
        _containerFilter = containerFilter;
    }

    @NotNull
    @Override
    public ContainerFilter getContainerFilter()
    {
        return _containerFilter;
    }

    @Override
    public boolean hasDefaultContainerFilter()
    {
        return false;
    }

    @Override
    public String getPublicSchemaName()
    {
        return AssaySchema.NAME;
    }
}
