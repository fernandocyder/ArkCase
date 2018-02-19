package com.armedia.acm.plugins.task.model;

import com.armedia.acm.core.model.AcmEvent;

public class BuckslipProcessStateEvent extends AcmEvent
{
    public enum BuckslipProcessState
    {
        INITIALIZED, WITHDRAWN, COMPLETED
    }

    private BuckslipProcessState buckslipProcessState;

    public BuckslipProcessStateEvent(Object source)
    {
        super(source);
    }

    public BuckslipProcessState getBuckslipProcessState()
    {
        return buckslipProcessState;
    }

    public void setBuckslipProcessState(BuckslipProcessState buckslipProcessState)
    {
        this.buckslipProcessState = buckslipProcessState;
    }
}