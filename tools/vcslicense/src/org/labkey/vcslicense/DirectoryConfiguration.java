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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
* User: adam
* Date: Mar 29, 2010
* Time: 8:49:17 AM
*/
public class DirectoryConfiguration implements Configuration
{
    private static final Set<String> MISSED_DIRECTORIES = Collections.newSetFromMap(new ConcurrentHashMap<>(20));
    private static final Set<String> MISSED_FILES = Collections.newSetFromMap(new ConcurrentHashMap<>(20));

    private final Collection<String> _includedDirectories;
    private final Set<String> _excludedDirectories;
    private final Set<String> _excludedFiles;
    private final RendererMap _renderers;

    DirectoryConfiguration(@NotNull Collection<String> includedDirectories, @Nullable Set<String> excludedDirectories, @Nullable Set<String> excludedFiles, @NotNull RendererMap renderers)
    {
        _includedDirectories = includedDirectories;
        _excludedDirectories = null != excludedDirectories ? excludedDirectories : Collections.<String>emptySet();
        _excludedFiles = null != excludedFiles ? excludedFiles : Collections.<String>emptySet();
        _renderers = renderers;

        MISSED_FILES.addAll(_excludedFiles);
        MISSED_DIRECTORIES.addAll(_excludedDirectories);
    }

    @Override
    public Collection<String> getIncludedDirectories()
    {
        return _includedDirectories;
    }

    @Override
    public boolean isAllowedDirectory(String dirPath)
    {
        if (!_excludedDirectories.contains(dirPath))
            return true;

        MISSED_DIRECTORIES.remove(dirPath);
        return false;
    }

    @Override
    public boolean isAllowedFile(String filePath)
    {
        if (!_excludedFiles.contains(filePath))
            return true;

        MISSED_FILES.remove(filePath);
        return false;
    }

    public static Collection<String> getMissedDirectories()
    {
        return MISSED_DIRECTORIES;
    }

    public static Collection<String> getMissedFiles()
    {
        return MISSED_FILES;
    }

    @Override
    public RendererMap getRenderers()
    {
        return _renderers;
    }

    @Override
    public Extractor getExtractor(ThreadContext context)
    {
        return new Extractor()
        {
            @Override
            public Date getYear1(String filePath, SVNLogEntry firstLogEntry)
            {
                return firstLogEntry.getDate();
            }

            @Override
            public Date getYear2(String filePath, SVNDirEntry entry)
            {
                return entry.getDate();
            }

            @Override
            public String getAuthor(String filePath, SVNLogEntry firstLogEntry)
            {
                return firstLogEntry.getAuthor();
            }
        };
    }
}
