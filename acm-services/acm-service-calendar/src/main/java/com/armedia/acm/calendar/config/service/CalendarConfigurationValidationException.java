package com.armedia.acm.calendar.config.service;

/**
 * @author Lazo Lazarev a.k.a. Lazarius Borg @ zerogravity Mar 21, 2017
 *
 */
public class CalendarConfigurationValidationException extends Exception
{

    private static final long serialVersionUID = 3670682245924498573L;

    private String objectType;

    public CalendarConfigurationValidationException(String message, String objectType)
    {
        super(message);
        this.objectType = objectType;
    }

    public CalendarConfigurationValidationException(Throwable t, String objectType)
    {
        super(t);
        this.objectType = objectType;
    }

    public CalendarConfigurationValidationException(String message, Throwable t, String objectType)
    {
        super(message, t);
        this.objectType = objectType;
    }

    /**
     * @return the objectType
     */
    public String getObjectType()
    {
        return objectType;
    }

}
