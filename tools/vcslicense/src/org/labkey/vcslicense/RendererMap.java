/*
 * Copyright (c) 2010-2016 LabKey Corporation
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
package org.labkey.vcslicense;

import org.labkey.vcslicense.renderers.LicenseRenderer;

import java.util.HashMap;

/**
* User: adam
* Date: Mar 29, 2010
* Time: 8:49:48 AM
*/
class RendererMap extends HashMap<String, LicenseRenderer>
{
    void addRenderer(LicenseRenderer renderer, String... extensions)
    {
        for (String extension : extensions)
            put(extension,  renderer);
    }
}
