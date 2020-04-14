/*
 * Copyright (c) 2015-2018 LabKey Corporation
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
 * Created by adam on 6/16/2015.
 */
public enum License
{
    Apache
    {
        @Override
        public String[] getLicenseText(String copyright, boolean longLicense)
        {
            return longLicense ? getLongLicenseText(copyright) : getShortLicenseText(copyright);
        }

        protected String[] getLongLicenseText(String copyright)
        {
            return new String[]
            {
                " " + copyright,
                "",
                " Licensed under the Apache License, Version 2.0 (the \\\"License\\\");",
                " you may not use this file except in compliance with the License.",
                " You may obtain a copy of the License at",
                "",
                "     http://www.apache.org/licenses/LICENSE-2.0",
                "",
                " Unless required by applicable law or agreed to in writing, software",
                " distributed under the License is distributed on an \\\"AS IS\\\" BASIS,",
                " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
                " See the License for the specific language governing permissions and",
                " limitations under the License.",
            };
        }

        protected String[] getShortLicenseText(String copyright)
        {
            return new String[]
            {
                " " + copyright,
                "",
                " Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0",
            };
        }
    },
    LabKey
    {
        @Override
        public String[] getLicenseText(String copyright, boolean longLicense)
        {
            return new String[]
            {
                    " " + copyright + ". All rights reserved. No portion of this work may be reproduced in",
                    " any form or by any electronic or mechanical means without written permission from LabKey Corporation.",
            };
        }
    },

    // Argos is a deprecated license include here to allow it to be detected and automatically replaced
    Argos
    {
        @Override
        public String[] getLicenseText(String copyright, boolean longLicense)
        {
            return new String[]
            {
                    " " + copyright,
                    "",
                    " This file is part of the Argos application, which cannot be copied or distributed without the express permission of LabKey Corporation",
            };
        }
    };

    abstract public String[] getLicenseText(String copyright, boolean longLicense);
}
