package org.labkey.security;

/**
* User: Mark Igra
* Date: Sep 12, 2007
* Time: 2:13:08 PM
*/
public class RemoteLoginException extends Exception
{
    RemoteLoginException(String message)
    {
        super(message);
    }

    RemoteLoginException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
