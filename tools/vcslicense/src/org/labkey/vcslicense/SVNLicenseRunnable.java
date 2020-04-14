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

import org.tmatesoft.svn.core.SVNException;

import java.io.IOException;

/**
 * User: adam
* Date: Mar 6, 2009
* Time: 8:15:58 AM
*/
public class SVNLicenseRunnable implements Runnable
{
    private final static ThreadLocal<ThreadContext> _contexts = new ThreadLocal<>();
    private final Repository _repository;
    private final Configuration _config;
    private final String _path;
    private final long _previousRevision;
    private final long _latestRevision;

    SVNLicenseRunnable(Repository repository, Configuration config, String path, long previousRevision, long latestRevision)
    {
        _repository = repository;
        _config = config;
        _path = path;
        _previousRevision = previousRevision;
        _latestRevision = latestRevision;
    }

    public void run()
    {
        try
        {
            VCSLicense.processSVNFiles(getContext(), _repository, _config, _path, _previousRevision, _latestRevision);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (SVNException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private ThreadContext getContext() throws IOException, SVNException, Exception
    {
        ThreadContext context = _contexts.get();

        if (null == context)
        {
            context = new ThreadContext(_repository);
            _contexts.set(context);
        }

        return context;
    }
}
