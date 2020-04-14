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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: adam
 * Date: 2/20/14
 * Time: 11:58 AM
 */
public class Repository
{
    private final String _name;
    private final String _svnServer;  // since git is distributed, it only works with local clones
    private final File _sourceDir;
    private final File _destinationDir;
    private final List<? extends Configuration> _configurations;
    private final LicenseSelector _licenseSelector;
    private final String _previousRevisionFilename;
    private final Set<String> _companiesToIgnore;
    private final String _branchToCompare;

    enum RepositoryType { GIT_REPOSITORY, SVN_REPOSITORY }

    private static final Set<String> MISSED_COMPANIES = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(20));

    static Set<String> getMissedCompanies()
    {
        return MISSED_COMPANIES;
    }

    // Don't need svnServer for git repos, and source directory is usually the same as destination directory
    public Repository(String name, File sourceDir, List<? extends Configuration> configurations, Set<String> companiesToIgnore, String previousRevisionFilename, LicenseSelector licenseSelector)
    {
        this(name, "", sourceDir, configurations, companiesToIgnore, previousRevisionFilename, licenseSelector);
    }

    // branchToCompare is often not overridden
    public Repository(String name, String svnServer, File sourceDir, List<? extends Configuration> configurations, Set<String> companiesToIgnore, String previousRevisionFilename,
                      LicenseSelector licenseSelector)
    {
        this(name, svnServer, sourceDir, configurations, companiesToIgnore, previousRevisionFilename, licenseSelector, "");
    }


    // Source directory is usually the same as destination directory
    public Repository(String name, String svnServer, File sourceDir, List<? extends Configuration> configurations, Set<String> companiesToIgnore, String previousRevisionFilename,
                      LicenseSelector licenseSelector, String branchToCompare)
    {
        this(name, svnServer, sourceDir, sourceDir, configurations, companiesToIgnore, previousRevisionFilename, licenseSelector, branchToCompare);
    }

    // Don't need svnServer for git repos
    public Repository(String name, File sourceDir, File destinationDir, List<? extends Configuration> configurations, Set<String> companiesToIgnore, String previousRevisionFilename,
                      LicenseSelector licenseSelector, String branchToCompare)
    {
        this(name, "", sourceDir, destinationDir, configurations, companiesToIgnore, previousRevisionFilename, licenseSelector, branchToCompare);
    }

    public Repository(String name, String svnServer, File sourceDir, File destinationDir, List<? extends Configuration> configurations, Set<String> companiesToIgnore, String previousRevisionFilename,
                      LicenseSelector licenseSelector, String branchToCompare)
    {
        _name = name;
        _svnServer = svnServer;
        _sourceDir = sourceDir;
        _destinationDir = destinationDir;
        _configurations = configurations;
        _companiesToIgnore = companiesToIgnore;
        _previousRevisionFilename = previousRevisionFilename;
        _licenseSelector = licenseSelector;
        _branchToCompare = branchToCompare;

        MISSED_COMPANIES.addAll(companiesToIgnore);
    }

    String getName()
    {
        return _name;
    }

    String getSvnServer()
    {
        return _svnServer;
    }

    File getSourceDir()
    {
        return _sourceDir;
    }

    File getDestinationDir()
    {
        return _destinationDir;
    }

    List<? extends Configuration> getConfigurations()
    {
        return _configurations;
    }

    LicenseSelector getLicenseSelector()
    {
        return _licenseSelector;
    }

    String getPreviousRevisionFilename()
    {
        return _previousRevisionFilename;
    }

    String getBranchToCompare()
    {
        return _branchToCompare;
    }

    boolean shouldIgnoreCompany(String companyName)
    {
        if (_companiesToIgnore.contains(companyName))
        {
            MISSED_COMPANIES.remove(companyName);
            return true;
        }

        return false;
    }

    static RepositoryType determineRepositoryType(File sourceDir)
    {
        File gitDirFile = new File(sourceDir, "/.git");
        if(gitDirFile.exists())
        {
            return Repository.RepositoryType.GIT_REPOSITORY;
        }
        else
        {
            return Repository.RepositoryType.SVN_REPOSITORY;
        }
    }
}
