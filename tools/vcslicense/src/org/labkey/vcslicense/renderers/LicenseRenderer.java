/*
 * Copyright (c) 2009-2018 LabKey Corporation
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
 * Date: Mar 5, 2009
 * Time: 10:39:18 PM
 */
public abstract class LicenseRenderer
{
    private final License _license;
    private boolean _longLicense = true;
    protected static final String CR = "\\r\\n";

    protected abstract String beginning();
    protected abstract String linePrefix();
    protected abstract String end();
    protected abstract int numberOfLinesPadding();

    public LicenseRenderer(License license)
    {
        _license = license;
    }

    public LicenseRenderer setShort()
    {
        _longLicense = false;
        return this;
    }

    public boolean shouldReport(Status status)
    {
        return (status != Status.Okay) && (status != Status.IgnoreCompany);
    }

    public void insertNewLicense(StringBuilder correction, String filePath, String license, String beginning)
    {
        correction.append("sed -i -b \"1i");   // -i means change the file in place, -b means leave the line endings alone
        correction.append(license);
        correction.append("\" \"").append(filePath).append("\"");
    }

    public void deleteOldLicense(StringBuilder correction, String filePath, License fileLicense, boolean fileLongLicense, String beginning)
    {
        /* TODO: JSPs will occasionally have a taglib line above the copyright, and this function will delete them.
           TODO: Someday either the JSPs or this function should be fixed.
         */

        if (fileLicense != null) {
            if (fileLicense == License.Apache) {
                if (fileLongLicense) {
                    // delete the top 13 lines + padding appropriate for file type
                    correction.append("sed -i -b \"1," + (13 + numberOfLinesPadding()) + "d\"");
                    correction.append(" \"").append(filePath).append("\"");
                } else {
                    // delete the top 3 lines + padding appropriate for file type
                    correction.append("sed -i -b \"1," + (3 + numberOfLinesPadding()) + "d\"");
                    correction.append(" \"").append(filePath).append("\"");
                }
            }
            if (fileLicense == License.Argos) {
                // delete the top 2 lines + padding appropriate for file type
                correction.append("sed -i -b \"1," + (3 + numberOfLinesPadding()) + "d\"");
                correction.append(" \"").append(filePath).append("\"");
            }
            if (fileLicense == License.LabKey) {
                // delete the top 2 lines + padding appropriate for file type
                correction.append("sed -i -b \"1," + (2 + numberOfLinesPadding()) + "d\"");
                correction.append(" \"").append(filePath).append("\"");
            }
        }
    }

    public void replaceLicense(StringBuilder correction, String filePath, License fileLicense, boolean fileLongLicense, String newLicense, String beginning)
    {
        deleteOldLicense (correction, filePath, fileLicense, fileLongLicense, beginning);
        correction.append("; ");
        insertNewLicense (correction, filePath, newLicense, beginning);
    }

    public boolean isWrongLicense(License fileLicense)
    {
        if (null != fileLicense)
        {
            // if the license in the file doesnt match the license for the repository
            return _license != fileLicense;
        }
        else
        {
            return true;
        }
    }

    public int getLicenseLengthForFileType()
    {
        return _license.getLicenseText("",_longLicense).length + numberOfLinesPadding();
    }

    public String getLicense(String copyright)
    {
        StringBuilder license = new StringBuilder();
        license.append(beginning()).append(CR);

        for (String line : _license.getLicenseText(copyright, _longLicense))
            license.append(linePrefix()).append(line).append(CR);

        license.append(end()).append("\\r");  // Just a carriage return -- SED will insert a line feed

        return license.toString();
    }
}
