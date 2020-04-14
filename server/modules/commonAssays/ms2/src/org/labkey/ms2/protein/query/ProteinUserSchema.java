/*
 * Copyright (c) 2015-2019 LabKey Corporation
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
package org.labkey.ms2.protein.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.collections.Sets;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.DataColumn;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.DisplayColumnFactory;
import org.labkey.api.data.RenderContext;
import org.labkey.api.data.TableInfo;
import org.labkey.api.module.Module;
import org.labkey.api.protein.ProteomicsModule;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QueryForeignKey;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.ms2.MS2Module;
import org.labkey.ms2.protein.ProteinManager;
import org.labkey.ms2.query.OrganismTableInfo;
import org.labkey.ms2.query.SequencesTableInfo;

import java.util.Set;

/**
 * User: kevink
 * Date: 4/20/15
 */
public class ProteinUserSchema extends UserSchema
{
    public static final String NAME = "protein";
    public static final String ANNOTATION_TABLE_NAME = TableType.Annotations.name();
    public static final String FASTA_FILE_TABLE_NAME = TableType.FastaFiles.name();
    public static final String SEQUENCES_TABLE_NAME = TableType.Sequences.name();

    public ProteinUserSchema(User user, Container container)
    {
        super(NAME, "Protein annotation, gene ontology, and sequence tables", user, container, ProteinManager.getSchema());
    }

    public static void register(MS2Module module)
    {
        DefaultSchema.registerProvider(NAME, new DefaultSchema.SchemaProvider(module)
        {
            @Override
            public boolean isAvailable(DefaultSchema schema, Module module)
            {
                // Publish schema if any ProteomicsModule is active in the container
                for (Module m : schema.getContainer().getActiveModules(schema.getUser()))
                {
                    if (m instanceof ProteomicsModule)
                        return true;
                }
                return false;
            }

            @Override
            public QuerySchema createSchema(DefaultSchema schema, Module module)
            {
                return new ProteinUserSchema(schema.getUser(), schema.getContainer());
            }
        });
    }

    public enum TableType
    {
        Annotations {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createAnnotationsTable();
            }
        },
        AnnotationTypes {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createAnnotationTypesTable();
            }
        },
        GoGraphPath {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createGoGraphPath();
            }
        },
        GoTerm {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createGoTerm();
            }
        },
        GoTerm2Term {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createGoTerm2Term();
            }
        },
        GoTermDefinition {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createGoTermDefinition();
            }
        },
        GoTermSynonym {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createGoTermSynonym();
            }
        },
        InfoSources {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createInfoSourcesTable();
            }
        },
        Organisms {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createOrganisms();
            }
        },
        Sequences {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createSequences();
            }
        },
        Identifiers {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createIdentifiersTable();
            }
        },
        IdentTypes {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createIdentTypesTable();
            }
        },
        FastaSequences {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createFastaSequencesTable();
            }
        },
        FastaFiles {
            @Override
            public TableInfo createTable(ProteinUserSchema schema, String name)
            {
                return schema.createFastaFileTable();
            }
        };

        public abstract TableInfo createTable(ProteinUserSchema schema, String name);
    }

    private TableInfo createFastaSequencesTable()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoFastaSequences(), null);
        table.init();
        table.setReadOnly(true);
        table.getMutableColumn("SeqId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.Sequences.name()) );
        table.getMutableColumn("FastaId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.FastaFiles.name()) );
        return table;
    }

    private TableInfo createIdentifiersTable()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoIdentifiers(), null);
        table.init();
        table.setReadOnly(true);
        table.getMutableColumn("SeqId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.Sequences.name()) );
        table.getMutableColumn("IdentTypeId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.IdentTypes.name()) );
        table.getMutableColumn("SourceId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.InfoSources.name()) );
        return table;
    }

    private TableInfo createIdentTypesTable()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoIdentTypes(), null);
        table.init();
        table.setReadOnly(true);
        table.getMutableColumn("CannonicalSourceId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.InfoSources.name()) );
        return table;
    }

    @Nullable
    @Override
    public TableInfo createTable(String name, ContainerFilter cf)
    {
        for (TableType tableType : TableType.values())
        {
            if (tableType.name().equalsIgnoreCase(name))
            {
                // ContainerFilter is not used
                return tableType.createTable(this, tableType.name());
            }
        }

        return null;
    }

    @Override
    public Set<String> getTableNames()
    {
        Set<String> result = Sets.newCaseInsensitiveHashSet();
        for (TableType tableType : TableType.values())
        {
            result.add(tableType.name());
        }
        return result;
    }

    private SimpleUserSchema.SimpleTable createAnnotationTypesTable()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoAnnotationTypes(), null);
        table.init();
        table.setReadOnly(true);
        table.getMutableColumn("SourceId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.InfoSources.name()) );
        return table;
    }

    private SimpleUserSchema.SimpleTable createInfoSourcesTable()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoInfoSources(), null);
        table.init();
        table.setReadOnly(true);
        return table;
    }

    protected TableInfo createAnnotationsTable()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoAnnotations(), null);
        table.init();
        table.setReadOnly(true);
        table.getMutableColumn("AnnotTypeId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.AnnotationTypes.name()) );
        table.getMutableColumn("AnnotSourceId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.InfoSources.name()) );
        table.getMutableColumn("AnnotIdent").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.Identifiers.name()) );
        table.getMutableColumn("SeqId").setFk( QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.Sequences.name()) );
        return table;
    }

    protected TableInfo createGoGraphPath()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoGoGraphPath(), null);
        table.init();
        table.setReadOnly(true);
        return table;
    }

    protected TableInfo createGoTerm()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoGoTerm(), null);
        table.init();
        table.setReadOnly(true);
        return table;
    }

    protected TableInfo createGoTerm2Term()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoGoTerm2Term(), null);
        table.init();
        table.setReadOnly(true);
        table.getMutableColumn("term1id").setFk(QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.GoTerm.name()) );
        table.getMutableColumn("term2id").setFk(QueryForeignKey.from(this, table.getContainerFilter()).table(TableType.GoTerm.name()) );
        return table;
    }

    protected TableInfo createGoTermDefinition()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoGoTermDefinition(), null);
        table.init();
        table.setReadOnly(true);
        return table;
    }

    protected TableInfo createGoTermSynonym()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoGoTermSynonym(), null);
        table.init();
        table.setReadOnly(true);
        return table;
    }

    protected TableInfo createOrganisms()
    {
        return new OrganismTableInfo(this);
    }

    protected SequencesTableInfo<ProteinUserSchema> createSequences()
    {
        // TODO ContainerFilter
        return new SequencesTableInfo<>(this, null);
    }

    protected TableInfo createFastaFileTable()
    {
        SimpleUserSchema.SimpleTable<ProteinUserSchema> table = new SimpleUserSchema.SimpleTable<>(this, ProteinManager.getTableInfoFastaFiles(), null);
        table.init();
        table.setReadOnly(true);
        var shortName = table.addWrapColumn("ShortName", table.getRealTable().getColumn("FileName"));
        shortName.setLabel("FASTA Name");

        shortName.setDisplayColumnFactory(new DisplayColumnFactory()
        {
            @Override
            public DisplayColumn createRenderer(ColumnInfo colInfo)
            {
                return new DataColumn(colInfo)
                {
                    @Override
                    public Object getValue(RenderContext ctx)
                    {
                        Object result = super.getValue(ctx);
                        if (result != null)
                        {
                            String s = result.toString().replace('\\', '/');
                            int index = s.lastIndexOf('/');
                            if (index != -1)
                            {
                                return s.substring(index + 1);
                            }
                        }
                        return super.getValue(ctx);
                    }

                    @NotNull
                    @Override
                    public String getFormattedValue(RenderContext ctx)
                    {
                        return PageFlowUtil.filter(getDisplayValue(ctx));
                    }

                    @Override
                    public Object getDisplayValue(RenderContext ctx)
                    {
                        return getValue(ctx);
                    }
                };
            }
        });
        return table;
    }

}
