/*
 * Copyright (c) 2007-2019 LabKey Corporation
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

package org.labkey.luminex;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.assay.AssayDefaultFlagHandler;
import org.labkey.api.assay.AssayFlagHandler;
import org.labkey.api.assay.AssayQCFlagColumn;
import org.labkey.api.data.Container;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.assay.AssayService;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.WebPartFactory;
import org.labkey.luminex.query.LuminexProtocolSchema;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class LuminexModule extends DefaultModule
{
    @Override
    public String getName()
    {
        return "Luminex";
    }

    @Override
    public @Nullable Double getSchemaVersion()
    {
        return 20.000;
    }

    @Override
    protected void init()
    {
        addController("luminex", LuminexController.class);
    }

    @Override
    @NotNull
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean hasScripts()
    {
        return true;
    }

    @Override
    @NotNull
    public Collection<String> getSummary(Container c)
    {
        return Collections.emptyList();
    }

    @Override
    public void doStartup(ModuleContext moduleContext)
    {
        ExperimentService.get().registerExperimentDataHandler(new LuminexDataHandler());
        AssayService.get().registerAssayProvider(new LuminexAssayProvider());
        PropertyService.get().registerDomainKind(new LuminexAnalyteDomainKind());
        PropertyService.get().registerDomainKind(new LuminexDataDomainKind());

        AssayFlagHandler.registerHandler(AssayService.get().getProvider(LuminexAssayProvider.NAME), new AssayDefaultFlagHandler());
    }

    @Override
    @NotNull
    public Set<String> getSchemaNames()
    {
        return Collections.singleton(LuminexProtocolSchema.DB_SCHEMA_NAME);
    }

    @NotNull
    @Override
    public Set<Class> getUnitTests()
    {
        return Set.of(
            AssayQCFlagColumn.TestCase.class,
            LuminexDataHandler.TestCase.class,
            LuminexExcelParser.TestCase.class,
            LuminexRunAsyncContext.TestCase.class,
            LuminexSaveExclusionsForm.TestCase.class
        );
    }
}
