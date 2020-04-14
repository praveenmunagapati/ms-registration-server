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
package org.labkey.microarray.matrix;

import org.labkey.api.data.AbstractTableInfo;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.exp.query.ExpSchema;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QueryForeignKey;
import org.labkey.api.assay.AssayProtocolSchema;
import org.labkey.microarray.query.MicroarrayUserSchema;

import java.util.ArrayList;
import java.util.List;

public class FeatureDataTable extends FilteredTable<ExpressionMatrixProtocolSchema>
{
    public FeatureDataTable(ExpressionMatrixProtocolSchema schema, ContainerFilter cf)
    {
        super(ExpressionMatrixProtocolSchema.getTableInfoFeatureData(), schema, cf);

        setDetailsURL(AbstractTableInfo.LINK_DISABLER);
        setImportURL(AbstractTableInfo.LINK_DISABLER);

        addColumn(wrapColumn(getRealTable().getColumn("RowId"))).setHidden(true);

        var valueColumn = addColumn(wrapColumn(getRealTable().getColumn("Value")));
        valueColumn.setHidden(false);
        valueColumn.setLabel("Value");
        valueColumn.setFormat("0.000000");

        var featureIdColumn = addColumn(wrapColumn(getRealTable().getColumn("FeatureId")));
        featureIdColumn.setHidden(false);
        featureIdColumn.setLabel("Probe Id");
        featureIdColumn.setFk(QueryForeignKey
                .from(schema, cf)
                .schema(MicroarrayUserSchema.SCHEMA_NAME)
                .to(MicroarrayUserSchema.TABLE_FEATURE_ANNOTATION, "RowId", "FeatureId"));

        var sampleIdColumn = addColumn(wrapColumn(getRealTable().getColumn("SampleId")));
        sampleIdColumn.setHidden(false);
        sampleIdColumn.setLabel("Sample Id");

        // Lookup to exp.materials since we don't know the sample set
        sampleIdColumn.setFk(QueryForeignKey
                .from(schema, cf)
                .schema(ExpSchema.SCHEMA_NAME)
                .to("materials", "RowId", "Name"));

        SQLFragment runSQL = new SQLFragment("(SELECT d.RunId FROM ");
        runSQL.append(ExperimentService.get().getTinfoData(), "d");
        runSQL.append(" WHERE d.RowId = ");
        runSQL.append(ExprColumn.STR_TABLE_ALIAS);
        runSQL.append(".DataId)");
        var runIdColumn = new ExprColumn(this, "Run", runSQL, JdbcType.INTEGER);
        addColumn(runIdColumn);
        runIdColumn.setFk(QueryForeignKey
                .from(getUserSchema(), getContainerFilter())
                .to(AssayProtocolSchema.RUNS_TABLE_NAME, "RowId", "Name"));

        var dataIdColumn = addColumn(wrapColumn(getRealTable().getColumn("DataId")));
        dataIdColumn.setHidden(false);
        dataIdColumn.setLabel("Data Id");
        dataIdColumn.setFk(QueryForeignKey
                .from(getUserSchema(), getContainerFilter())
                .schema(ExpSchema.SCHEMA_NAME, schema.getContainer())
                .to( ExpSchema.TableType.Data.name(),"RowId", "Name"));

        List<FieldKey> columns = new ArrayList<>(getDefaultVisibleColumns());
        columns.remove(dataIdColumn.getFieldKey());
        setDefaultVisibleColumns(columns);

        // Issue 21134: filter by assay protocol
        SQLFragment filter = new SQLFragment("DataId");
        filter.append(" IN (SELECT d.RowId FROM ");
        filter.append(ExperimentService.get().getTinfoData(), "d");
        filter.append(", ");
        filter.append(ExperimentService.get().getTinfoExperimentRun(), "r");
        filter.append(" WHERE d.RunId = r.RowId");
        if (schema.getProtocol() != null)
        {
            filter.append(" AND r.ProtocolLSID = ?");
            filter.add(schema.getProtocol().getLSID());
        }
        filter.append(") ");

        addCondition(filter, FieldKey.fromParts("DataId"));
    }

    @Override
    protected void applyContainerFilter(ContainerFilter filter)
    {
        // There isn't a container column directly on this table so do a special filter
        if (getContainer() != null)
        {
            FieldKey containerColumn = FieldKey.fromParts("Run", "Folder");
            clearConditions(containerColumn);
            addCondition(filter.getSQLFragment(getSchema(), new SQLFragment("(SELECT d.Container FROM exp.Data d WHERE d.RowId = DataId)"), getContainer()), containerColumn);
        }
    }

}
