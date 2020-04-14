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
package org.labkey.vcslicense.renderers;

/**
 * User: adam
 * Date: Mar 24, 2010
 * Time: 3:49:33 PM
 */
public class RLicenseRenderer extends LicenseRenderer
{
    public RLicenseRenderer(License license)
    {
        super(license);
    }

    protected String beginning()
    {
        return "##";
    }

    protected String linePrefix()
    {
        return "# ";
    }

    protected String end()
    {
        return "##";
    }

    @Override
    protected int numberOfLinesPadding() {
        return 2;
    }

}