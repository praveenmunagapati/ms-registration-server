/*
 * Copyright (c) 2018-2019 LabKey Corporation
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
package org.labkey.experiment.api;

import org.labkey.api.data.BaseColumnInfo;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.query.ExpDataProtocolInputTable;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.UserSchema;

public class ExpDataProtocolInputTableImpl extends ExpProtocolInputTableImpl<ExpDataProtocolInputTable.Column> implements ExpDataProtocolInputTable
{
    protected ExpDataProtocolInputTableImpl(String name, UserSchema schema, ContainerFilter cf)
    {
        super(name, ExperimentServiceImpl.get().getTinfoProtocolInput(), schema, cf);

        getFilter().addCondition(FieldKey.fromParts("objectType"), ExpData.DEFAULT_CPAS_TYPE);
    }

    @Override
    protected void populateColumns()
    {
        addColumn(Column.RowId);
        addColumn(Column.Name);
        setTitleColumn(Column.Name.toString());
        addColumn(Column.LSID);
        addColumn(Column.Protocol);
        addColumn(Column.Input);
        addColumn(Column.DataClass);
        addColumn(Column.MinOccurs);
        addColumn(Column.MaxOccurs);
    }

    @Override
    public BaseColumnInfo createColumn(String alias, Column column)
    {
        switch (column)
        {
            case RowId:
                return createRowIdColumn(alias);
            case Name:
                return createNameColumn(alias);
            case LSID:
                return createLsidColumn(alias);
            case Protocol:
                return createProtocolColumn(alias);
            case Input:
                return createInputColumn(alias);
            case DataClass:
                return createDataClassColumn(alias);
            case MinOccurs:
                return createMinOccursColumn(alias);
            case MaxOccurs:
                return createMaxOccursColumn(alias);
        }
        throw new IllegalArgumentException("Unknown column " + column);
    }
}