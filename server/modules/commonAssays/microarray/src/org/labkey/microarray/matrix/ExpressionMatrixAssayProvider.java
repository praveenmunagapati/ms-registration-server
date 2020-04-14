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

import org.apache.commons.lang3.StringUtils;
import org.fhcrc.cpas.exp.xml.ExperimentRunType;
import org.fhcrc.cpas.exp.xml.SimpleTypeNames;
import org.fhcrc.cpas.exp.xml.SimpleValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.TableSelector;
import org.labkey.api.exp.PropertyType;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.exp.api.ExperimentUrls;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.exp.property.Lookup;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.pipeline.PipelineProvider;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QueryService;
import org.labkey.api.security.User;
import org.labkey.api.assay.actions.AssayRunUploadForm;
import org.labkey.api.assay.AbstractAssayProvider;
import org.labkey.api.assay.AssayDataType;
import org.labkey.api.assay.AssayPipelineProvider;
import org.labkey.api.assay.AssayProtocolSchema;
import org.labkey.api.assay.AssayRunCreator;
import org.labkey.api.assay.AssayTableMetadata;
import org.labkey.api.study.assay.ParticipantVisitResolverType;
import org.labkey.api.assay.matrix.ColumnMappingProperty;
import org.labkey.api.util.FileType;
import org.labkey.api.util.HtmlString;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.Pair;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.HttpView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.ViewContext;
import org.labkey.microarray.MicroarrayModule;
import org.labkey.microarray.controllers.FeatureAnnotationSetController;
import org.labkey.microarray.query.MicroarrayUserSchema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExpressionMatrixAssayProvider extends AbstractAssayProvider
    {
    public static final String NAME = "Expression Matrix";
    public static final String RESOURCE_NAME = "ExpressionMatrix";

    public static final String LSID_PREFIX = "ExpressionMatrix";
    public static final AssayDataType DATA_TYPE = new AssayDataType(LSID_PREFIX, new FileType(".tsv"));

    public static final String FEATURE_SET_PROPERTY_NAME = "featureSet";

    public static final ColumnMappingProperty FEATURE_ANNOTATION_SET_ID_COLUMN = new ColumnMappingProperty(FEATURE_SET_PROPERTY_NAME, "Feature Annotation Set", true);
    public static final ColumnMappingProperty IMPORT_VALUES_COLUMN = new ColumnMappingProperty("importValues", "Import Values", false);

    public ExpressionMatrixAssayProvider()
    {
        super(LSID_PREFIX, LSID_PREFIX, DATA_TYPE, ModuleLoader.getInstance().getModule(MicroarrayModule.class));
    }

    @Override @NotNull
    public AssayTableMetadata getTableMetadata(@NotNull ExpProtocol protocol)
    {
        return new AssayTableMetadata(this, protocol, null, FieldKey.fromParts("Run"), FieldKey.fromParts("RowId"));
    }

    @Override
    public AssayRunCreator getRunCreator()
    {
        return new ExpressionMatrixRunCreator(this);
    }

    @Override
    public ExpData getDataForDataRow(Object dataRowId, ExpProtocol protocol)
    {
        return null;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getResourceName()
    {
        return RESOURCE_NAME;
    }

    @Override
    public HttpView getDataDescriptionView(AssayRunUploadForm form)
    {
        return new HtmlView(HtmlString.EMPTY_STRING);
    }

    @Override
    protected Pair<Domain, Map<DomainProperty, Object>> createBatchDomain(Container c, User user)
    {
        return super.createBatchDomain(c, user, false);
    }

    @Override
    public AssayProtocolSchema createProtocolSchema(User user, Container container, @NotNull ExpProtocol protocol, @Nullable Container targetStudy)
    {
        return new ExpressionMatrixProtocolSchema(user, container, this, protocol, targetStudy);
    }

    @Override
    public List<ParticipantVisitResolverType> getParticipantVisitResolverTypes()
    {
        return Collections.emptyList();
    }

    @Override
    public Domain getResultsDomain(ExpProtocol protocol)
    {
        return null;
    }

    @Override
    public PipelineProvider getPipelineProvider()
    {
        return new AssayPipelineProvider(MicroarrayModule.class,
                new PipelineProvider.FileTypesEntryFilter(getDataType().getFileType()),
                this, "Import Expression Matrix");
    }

    @Override
    public String getDescription()
    {
        return "Import a GEO series matrix-like TSV file of probe/sample values.";
    }

    @Override
    public List<NavTree> getHeaderLinks(ViewContext viewContext, ExpProtocol protocol, ContainerFilter containerFilter)
    {
        List<NavTree> result = super.getHeaderLinks(viewContext, protocol, containerFilter);
        result.add(new NavTree("manage feature annotation sets", new ActionURL(FeatureAnnotationSetController.ManageAction.class, viewContext.getContainer())));
        result.add(new NavTree("manage samples", PageFlowUtil.urlProvider(ExperimentUrls.class).getShowSampleSetListURL(viewContext.getContainer())));
        return result;
    }

    @Override
    protected Pair<Domain, Map<DomainProperty, Object>> createRunDomain(Container c, User user)
    {
        Pair<Domain, Map<DomainProperty, Object>> result = super.createRunDomain(c, user);
        Domain runDomain = result.getKey();

        DomainProperty featureSet = addProperty(runDomain, FEATURE_ANNOTATION_SET_ID_COLUMN.getName(), FEATURE_ANNOTATION_SET_ID_COLUMN.getLabel(), PropertyType.INTEGER);
        featureSet.setLookup(new Lookup(null, MicroarrayUserSchema.SCHEMA_NAME, MicroarrayUserSchema.TABLE_FEATURE_ANNOTATION_SET));
        featureSet.setShownInInsertView(true);
        featureSet.setShownInUpdateView(false);
        featureSet.setRequired(true);

        DomainProperty importValues = addProperty(runDomain, IMPORT_VALUES_COLUMN.getName(), IMPORT_VALUES_COLUMN.getLabel(), PropertyType.BOOLEAN);
        importValues.setShownInInsertView(true);
        importValues.setShownInUpdateView(false);
        importValues.setShownInDetailsView(false);
        importValues.setHidden(true);
        importValues.setRequired(false);

        // Default the ImportValues column as true by default
        Map<DomainProperty, Object> defaultValues = new HashMap<>();
        defaultValues.put(importValues, Boolean.TRUE);

        result.setValue(defaultValues);

        return result;
    }

    @Override
    protected Map<String, Set<String>> getRequiredDomainProperties()
    {
        Map<String, Set<String>> domainMap = super.getRequiredDomainProperties();
        Set<String> runProperties = domainMap.computeIfAbsent(ExpProtocol.ASSAY_DOMAIN_RUN, k -> new HashSet<>());

        runProperties.add(FEATURE_ANNOTATION_SET_ID_COLUMN.getName());
        runProperties.add(IMPORT_VALUES_COLUMN.getName());

        return domainMap;
    }


    @Override
    public XarCallbacks getXarCallbacks(final User user, final Container container)
    {
        return new XarCallbacks()
        {
            Map<Integer,String> mapRowIdName = null;
            Map<String,Integer> mapNameRowId = null;

            @Override
            public void beforeXarExportRun(ExpRun run, ExperimentRunType xrun)
            {
                if (null == mapRowIdName)
                {
                    // get map featureset.rowid->featureset.name
                    QuerySchema microarray = DefaultSchema.get(user, container).getSchema("Microarray");
                    if (null == microarray)
                        return;
                    Map map = QueryService.get().selector(microarray, "SELECT RowId, Name FROM FeatureAnnotationSet")
                            .getValueMap();
                    mapRowIdName = (Map<Integer,String>)map;
                    // Make sure this is really <Integer,String>?
                    if (!mapRowIdName.isEmpty())
                    {
                        Map.Entry<Integer,String> e = mapRowIdName.entrySet().iterator().next();
                        assert e.getKey() instanceof Integer;
                        assert e.getValue() instanceof String;
                    }
                }
                for (SimpleValueType sv : xrun.getProperties().getSimpleValArray())
                {
                    if (StringUtils.equals("featureSet",sv.getName()))
                    {
                        int featureAnnotationSetRowId = (int)Float.parseFloat(sv.getStringValue());
                        String featureAnnotationSetName = mapRowIdName.get(featureAnnotationSetRowId);
                        sv.setValueType(SimpleTypeNames.STRING);
                        sv.setStringValue(featureAnnotationSetName);
                    }
                }
            }

            @Override
            public void beforeXarImportRun(ExperimentRunType xrun)
            {
                if (null == mapNameRowId)
                {
                    // get map featureset.rowid->featureset.name
                    QuerySchema microarray = DefaultSchema.get(user, container).getSchema("Microarray");
                    if (null == microarray)
                        return;
                    Map map = QueryService.get().selector(microarray, "SELECT Name, RowId FROM FeatureAnnotationSet")
                            .getValueMap();
                    mapNameRowId = (Map<String,Integer>)map;
                    // Make sure this is really <Integer,String>?
                    if (!mapNameRowId.isEmpty())
                    {
                        Map.Entry<String,Integer> e = mapNameRowId.entrySet().iterator().next();
                        assert e.getKey() instanceof String;
                        assert e.getValue() instanceof Integer;
                    }
                }
                for (SimpleValueType sv : xrun.getProperties().getSimpleValArray())
                {
                    if (StringUtils.equals(FEATURE_SET_PROPERTY_NAME,sv.getName()))
                    {
                        String featureAnnotationSetName = sv.getStringValue();
                        Integer featureAnnotationRowId = mapNameRowId.get(featureAnnotationSetName);
                        sv.setValueType(SimpleTypeNames.INTEGER);
                        sv.setStringValue(featureAnnotationRowId==null ? null : String.valueOf(featureAnnotationRowId));
                    }
                }
            }
        };
    }

        @Override
        public Long getResultRowCount(List<? extends ExpProtocol> protocols)
        {
            return new TableSelector(ExpressionMatrixProtocolSchema.getTableInfoFeatureData()).getRowCount();
        }
    }
