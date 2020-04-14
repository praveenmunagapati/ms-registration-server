/*
 * Copyright (c) 2009-2016 LabKey Corporation
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

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.SVNException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: adam
 * Date: Mar 6, 2009
 * Time: 11:30:43 AM
 */

// Holds members we want to initialize just once per thread.
public class ThreadContext
{
    private static final int BUFFER_SIZE = 16*1024;
    private final char[] buffer = new char[BUFFER_SIZE];
    private final DateFormat year = new SimpleDateFormat("yyyy");
    private final Repository _repository;
    private org.eclipse.jgit.lib.Repository _gitRepository;
    private SVNRepository _svnRepository;
    private Repository.RepositoryType _repositoryType;

    public ThreadContext(Repository repository) throws IOException, SVNException, Exception
    {
        _repository = repository;

        File gitDirFile = repository.getSourceDir();
        _repositoryType = Repository.determineRepositoryType(gitDirFile);
        gitDirFile = new File(repository.getSourceDir(), "/.git");  // add for getGitRepository()
        if(_repositoryType == Repository.RepositoryType.GIT_REPOSITORY)
        {
            _gitRepository = VCSLicense.getGitRepository(repository, gitDirFile);
        }
        else if(_repositoryType == Repository.RepositoryType.SVN_REPOSITORY)
        {
            _svnRepository = VCSLicense.getSVNRepository(repository);
        }
        else   // should never happen, just being paranoid
        {
            throw new Exception("Unexpected repository type for repo '" + repository.getName() + "'.");
        }
    }

    public Repository getRepository()
    {
        return _repository;
    }

    public org.eclipse.jgit.lib.Repository getGitRepository() { return _gitRepository; }

    public SVNRepository getSVNRepository() { return _svnRepository; }

    public Repository.RepositoryType getRepositoryType() { return _repositoryType; }

    public String getBeginning(File file) throws IOException
    {
        try (BOMInputStream bis = new BOMInputStream(new FileInputStream(file), ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE))
        {
            ByteOrderMark bom = bis.getBOM();

            if (null != bom)
                throw new IOException("Byte Order Mark (BOM) detected at the start of \"" + file.getAbsolutePath() + "\"");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8), BUFFER_SIZE))
            {
                int chars = reader.read(buffer, 0, BUFFER_SIZE);

                // Skip empty files
                if (-1 == chars)
                    return null;

                return new String(buffer, 0, chars);
            }
        }
    }

    public String getYear(Date d)
    {
        return year.format(d);
    }
}
