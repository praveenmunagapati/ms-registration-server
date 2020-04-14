/*
 * Copyright (c) 2018 LabKey Corporation
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
package org.labkey.flow;

import org.labkey.api.data.DeferredUpgrade;
import org.labkey.api.data.UpgradeCode;
import org.labkey.api.module.ModuleContext;
import org.labkey.flow.persist.FlowManager;

public class FlowUpgradeCode implements UpgradeCode
{
    // called from flow-18.20-18.21.sql
    @DeferredUpgrade // must run after startup so the FlowProperty.FileDate has been created
    public static void addFileDate(ModuleContext context)
    {
        if (context.isNewInstall())
            return;

        FlowManager.get().setFileDateForAllFCSFiles(context.getUpgradeUser());
    }
}
