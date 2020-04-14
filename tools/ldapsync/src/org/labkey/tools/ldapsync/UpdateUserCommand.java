package org.labkey.tools.ldapsync;

import org.json.simple.JSONObject;
import org.labkey.remoteapi.CommandResponse;
import org.labkey.remoteapi.PostCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * User: marki
 * Date: Mar 3, 2010
 * Time: 3:38:57 PM
 */
public class UpdateUserCommand extends PostCommand<UpdateUserResponse> {
    LabKeyUserRecord newValues;

    public UpdateUserCommand(LabKeyUserRecord newValues)
    {
        super("user", "showUpdate");
        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (UserField uf : UserField.values())
            if (null != uf.getFormName())
                paramMap.put(uf.getFormName(), newValues.getUserField(uf));
        paramMap.put("UserId", newValues.getUserId());
        setParameters(paramMap);
    }

    @Override
    protected UpdateUserResponse createResponse(String text, int status, String contentType, JSONObject json) {
        return new UpdateUserResponse(text, status, contentType, json, this);
    }
}
