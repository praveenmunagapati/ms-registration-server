package org.scharp.atlas.peptide;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DbSchema;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.BaseWebPartFactory;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.WebPartFactory;
import org.labkey.api.view.WebPartView;
import org.scharp.atlas.peptide.view.PeptideWebPart;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class PeptideModule extends DefaultModule
{
    private static final Logger _log = Logger.getLogger(DefaultModule.class);
    public static final String NAME = "Peptide";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public @Nullable Double getSchemaVersion()
    {
        return 0.09;
    }

    @Override
    protected void init()
    {
       addController("peptide", PeptideController.class);
    }

    @Override
    @NotNull
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return Arrays.asList(new BaseWebPartFactory("Peptide Summary", WebPartFactory.LOCATION_BODY, WebPartFactory.LOCATION_RIGHT)
        {
            {
                addLegacyNames("Narrow Peptide Summary");
            }

            @Override
            public WebPartView getWebPartView(@NotNull ViewContext portalCtx, @NotNull Portal.WebPart webPart)
            {
                return new PeptideWebPart();
            }
        });
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
        // add a container listener so we'll know when our container is deleted:
        ContainerManager.addContainerListener(new PeptideContainerListener());
    }
    @Override
    @NotNull
    public Set<String> getSchemaNames()
    {
        return PageFlowUtil.set(PeptideSchema.getInstance().getSchemaName());
    }

    @Override
    @NotNull
    public Set<DbSchema> getSchemasToTest()
    {
        return PageFlowUtil.set(PeptideSchema.getInstance().getSchema());
    }
}
