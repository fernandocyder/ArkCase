package com.armedia.acm.services.transcribe.model;

/**
 * Created by Riste Tutureski <riste.tutureski@armedia.com> on 02/27/2018
 */
public enum ActionType
{
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}