/*
 * Copyright (c) 2008-2018 LabKey Corporation
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

package org.labkey.ms2.pipeline.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializableException;
import org.labkey.ms2.pipeline.client.GWTSearchServiceResult;


/**
 * User: billnelson@uky.edu
 * Date: Jan 29, 2008
 */
public interface SearchService extends RemoteService
{
    public GWTSearchServiceResult getSearchServiceResult(String searchEngine, 
                                                  String path, String[] fileNames);

    public GWTSearchServiceResult getSequenceDbs(String defaultDb, String searchEngine, boolean refresh)
            ;

    public GWTSearchServiceResult getProtocol(String searchEngine, String protocolName, String path, String[] fileNames)
            ;

    public GWTSearchServiceResult getMascotTaxonomy(String searchEngine);

    public GWTSearchServiceResult getEnzymes(String searchEngine);
}
