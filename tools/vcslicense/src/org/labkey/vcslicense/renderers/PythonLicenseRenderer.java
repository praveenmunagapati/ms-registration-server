/*
 * Copyright (c) 2011-2016 LabKey Corporation
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
public class PythonLicenseRenderer extends LicenseRenderer
{
    public PythonLicenseRenderer(License license)
    {
        super(license);
    }

    protected String beginning()
    {
        return "#";
    }

    protected String linePrefix()
    {
        return "#";
    }

    protected String end()
    {
        return "#";
    }

    @Override
    public void insertNewLicense(StringBuilder correction, String filePath, String license, String beginning)
    {
        if (beginning.startsWith("#!"))
        {
            // Leave the first two lines alone... insert license on row 3. This assumes line 2 is blank, but there seems to be
            // no good way to insert text that starts with a blank line.
            correction.append("sed -i -b \"3i\\");   // -i means change the file in place, -b means leave the line endings alone
            correction.append(license);
            correction.append("\\n");
            correction.append("\" \"").append(filePath).append("\"");
        }
        else
        {
            super.insertNewLicense(correction, filePath, license, beginning);
        }
    }

    @Override
    public void deleteOldLicense(StringBuilder correction, String filePath, License fileLicense, boolean fileLongLicense, String beginning)
    {
        if (beginning.startsWith("#!"))
        {
            // Leave the first two lines alone... assume old license starts on row 3
            if (fileLicense != null) {
                // delete the old license
                if (fileLicense == License.Apache) {
                    if (fileLongLicense) {
                        // delete 13 lines + padding appropriate for file type
                        correction.append("sed -i -b \"3," + (15 + numberOfLinesPadding()) + "d\"");
                        correction.append(" \"").append(filePath).append("\"");
                    } else {
                        // delete the top 3 lines + padding appropriate for file type
                        correction.append("sed -i -b \"3," + (6 + numberOfLinesPadding()) + "d\"");
                        correction.append(" \"").append(filePath).append("\"");
                    }
                }
                if (fileLicense == License.LabKey) {
                    // delete the top 2 lines + padding appropriate for file type
                    correction.append("sed -i -b \"3," + (5 + numberOfLinesPadding()) + "d\"");
                    correction.append(" \"").append(filePath).append("\"");
                }
            }
        }
        else
        {
            super.deleteOldLicense(correction, filePath, fileLicense, fileLongLicense, beginning);
        }
    }


    @Override
    protected int numberOfLinesPadding() {
        return 2;
    }

}