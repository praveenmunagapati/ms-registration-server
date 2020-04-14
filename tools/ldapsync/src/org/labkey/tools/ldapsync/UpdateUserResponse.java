package org.labkey.tools.ldapsync;

import org.labkey.remoteapi.Command;
import org.labkey.remoteapi.CommandResponse;
import org.json.simple.JSONObject;

/**
 * User: marki
 * Date: Mar 27, 2010
 * Time: 5:15:19 PM
 */
public class UpdateUserResponse extends CommandResponse {
    public UpdateUserResponse(String text, int statusCode, String contentType, JSONObject json, Command sourceCommand) {
        super(text, statusCode, contentType, json, sourceCommand);
    }

    public boolean succeeded()
    {
        return getStatusCode() == 301 || getStatusCode() == 302; //A redirect will occur on success only.
    }
}
