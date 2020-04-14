/*
 * Copyright (c) 2014-2016 LabKey Corporation
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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * User: adam
 * Date: 2/20/14
 * Time: 1:36 PM
 */
public class LabKeyLicenseSelector implements LicenseSelector
{
    private final String fhcrcName = "Fred Hutchinson Cancer Research Center";
    private final Set<String> fhcrc = new HashSet<>(Arrays.asList("tholzman", "mfitzgib", "dhmay@fhcrc.org", "v.obenchain"));
    private final Set<String> fhcrcAndLabkey = new HashSet<>(Arrays.asList("arauch", "mbellew", "bmaclean", "bmclean", "migra"));
    private final Date startDate;

    LabKeyLicenseSelector()
    {
        try
        {
            startDate = DateFormat.getDateInstance(DateFormat.SHORT).parse("10/1/05");
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isValidCompany(String fileCompany)
    {
        return fileCompany.equals(fhcrcName) || fileCompany.equals(VCSLicense.labkeyName);
    }

    @Override
    public String getCompany(Date createDate, String fileAuthor)
    {
        if (fhcrc.contains(fileAuthor))
            return fhcrcName;

        if (fhcrcAndLabkey.contains(fileAuthor))
            return (createDate.before(startDate) ? fhcrcName : VCSLicense.labkeyName);

        return VCSLicense.labkeyName;
    }

    // Used to detect licenses that don't follow standard copyright patterns
    private static final Pattern licensesToIgnore = Pattern.compile("Licensed to the Apache Software Foundation \\(ASF\\)");

    @Override
    public boolean shouldIgnoreFile(String beginning)
    {
        return licensesToIgnore.matcher(beginning).find();
    }
}
