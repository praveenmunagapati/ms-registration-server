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
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.MultiValuedForeignKey;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.exp.PropertyDescriptor;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.LookupForeignKey;
import org.labkey.api.query.PropertyForeignKey;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.RowIdQueryUpdateService;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.api.assay.AbstractAssayProvider;
import org.labkey.api.assay.AssaySchema;
import org.labkey.luminex.LuminexAssayProvider;
import org.labkey.luminex.model.Analyte;

import java.util.Map;
import java.util.TreeMap;

/**
 * User: jeckels
 * Date: 7/8/11
 */
public class AnalyteTable extends AbstractLuminexTable
{
    public AnalyteTable(final LuminexProtocolSchema schema, ContainerFilter cf, boolean filter)
    {
        super(LuminexProtocolSchema.getTableInfoAnalytes(), schema, cf, filter);
        setName(LuminexProtocolSchema.ANALYTE_TABLE_NAME);
        setPublicSchemaName(AssaySchema.NAME);
        
        addColumn(wrapColumn(getRealTable().getColumn("Name")));
        addColumn(wrapColumn("Data", getRealTable().getColumn("DataId"))).setFk(new LookupForeignKey(cf,"RowId", null)
        {
            public TableInfo getLookupTableInfo()
            {
                return _userSchema.createDataFileTable(getLookupContainerFilter());
            }
        });
        addColumn(wrapColumn(getRealTable().getColumn("RowId"))).setHidden(true);
        addColumn(wrapColumn(getRealTable().getColumn("FitProb")));
        addColumn(wrapColumn(getRealTable().getColumn("ResVar")));
        addColumn(wrapColumn(getRealTable().getColumn("RegressionType")));
        addColumn(wrapColumn(getRealTable().getColumn("StdCurve")));
        addColumn(wrapColumn(getRealTable().getColumn("PositivityThreshold")));
        addColumn(wrapColumn(getRealTable().getColumn("NegativeBead")));

        var titrationColumn = addColumn(wrapColumn("Standard", getRealTable().getColumn("RowId")));
        titrationColumn.setFk(new MultiValuedForeignKey(new LookupForeignKey(cf,"Analyte", null)
        {
            @Override
            public TableInfo getLookupTableInfo()
            {
                final FilteredTable result = new FilteredTable<>(LuminexProtocolSchema.getTableInfoAnalyteTitration(), schema, getLookupContainerFilter());
                var titrationColumn = result.addColumn(result.wrapColumn("Titration", result.getRealTable().getColumn("TitrationId")));
                titrationColumn.setFk(new LookupForeignKey(cf,"RowId", null)
                {
                    @Override
                    public TableInfo getLookupTableInfo()
                    {
                        TitrationTable titrationTable = _userSchema.createTitrationTable(getLookupContainerFilter(), false);
                        titrationTable.addCondition(new SimpleFilter(FieldKey.fromParts("Standard"), Boolean.TRUE));
                        return titrationTable;
                    }
                });
                var analyteColumn = result.addColumn(result.wrapColumn("Analyte", result.getRealTable().getColumn("AnalyteId")));
                analyteColumn.setFk(new LookupForeignKey(cf,"RowId", null)
                {
                    @Override
                    public TableInfo getLookupTableInfo()
                    {
                        return _userSchema.createAnalyteTable(getLookupContainerFilter(), false);
                    }
                });
                return result;
            }
        }, "Titration"));
        titrationColumn.setHidden(false);

        var lsidColumn = addColumn(wrapColumn(getRealTable().getColumn("LSID")));
        lsidColumn.setHidden(true);
        lsidColumn.setShownInInsertView(false);
        lsidColumn.setShownInUpdateView(false);

        var colProperty = wrapColumn("Properties", getRealTable().getColumn("LSID"));
        Domain analyteDomain = AbstractAssayProvider.getDomainByPrefix(_userSchema.getProtocol(), LuminexAssayProvider.ASSAY_DOMAIN_ANALYTE);
        Map<String, PropertyDescriptor> map = new TreeMap<>();
        for(DomainProperty pd : analyteDomain.getProperties())
        {
            map.put(pd.getName(), pd.getPropertyDescriptor());
        }
        colProperty.setFk(new PropertyForeignKey(_userSchema, getContainerFilter(), map));
        colProperty.setIsUnselectable(true);
        colProperty.setReadOnly(true);
        colProperty.setShownInInsertView(false);
        colProperty.setShownInUpdateView(false);
        addColumn(colProperty);

        String beadNumberField = ExprColumn.STR_TABLE_ALIAS + ".beadNumber";
        String beadNumber = "CASE WHEN ("+beadNumberField+" is not null and replace("+beadNumberField+",' ','') <> '') THEN CONCAT(' (', "+beadNumberField+",')') END";
        SQLFragment analyteBeadNameSQL = new SQLFragment("CONCAT(" + ExprColumn.STR_TABLE_ALIAS + ".name," + beadNumber + ")");
        ExprColumn analyteBeadNameCol = new ExprColumn(this, "AnalyteWithBead", analyteBeadNameSQL, JdbcType.VARCHAR);
        addColumn(analyteBeadNameCol);
        analyteBeadNameCol.setReadOnly(true);

        addColumn(wrapColumn(getRealTable().getColumn("BeadNumber")));
    }

    @Override
    protected SQLFragment createContainerFilterSQL(ContainerFilter filter, Container container)
    {
        SQLFragment sql = new SQLFragment("DataId IN (SELECT RowId FROM ");
        sql.append(ExperimentService.get().getTinfoData(), "d");
        sql.append(" WHERE ");
        sql.append(filter.getSQLFragment(getSchema(), new SQLFragment("Container"), container));
        sql.append(")");
        return sql;
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return (perm.equals(UpdatePermission.class) || perm.equals(ReadPermission.class))
                && _userSchema.getContainer().hasPermission(user, perm);
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new RowIdQueryUpdateService<Analyte>(this)
        {
            @Override
            public Analyte get(User user, Container container, int key)
            {
                return new TableSelector(LuminexProtocolSchema.getTableInfoAnalytes()).getObject(key, Analyte.class);
            }

            @Override
            public void delete(User user, Container container, int key)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected Analyte createNewBean()
            {
                return new Analyte();
            }

            @Override
            protected Analyte insert(User user, Container container, Analyte bean)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected Analyte update(User user, Container container, Analyte newAnalyte, Integer oldKey)
            {
                return Table.update(user, LuminexProtocolSchema.getTableInfoAnalytes(), newAnalyte, oldKey);
            }
        };
    }
}
