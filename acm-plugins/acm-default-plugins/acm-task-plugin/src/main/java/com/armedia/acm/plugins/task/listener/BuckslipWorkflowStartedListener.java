package com.armedia.acm.plugins.task.listener;

import com.armedia.acm.plugins.task.model.TaskConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmiller on 1/16/2017.
 */
public class BuckslipWorkflowStartedListener implements ExecutionListener
{
    private transient final Logger LOG = LoggerFactory.getLogger(getClass());

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception
    {
        delegateExecution.setVariable("isBuckslipWorkflow", Boolean.TRUE);

        // initialize past approvers to "[]" (JSON empty array)
        delegateExecution.setVariable("pastApprovers", "[]");

        copyApproversToFutureApproversIfNeeded(delegateExecution);

        moveFirstFutureApproverToCurrentApproverIfNeeded(delegateExecution);

        // set moreApprovers true if we have a current approver
        String currentApprover = (String) delegateExecution.getVariable("currentApprover");
        String moreApprovers = currentApprover == null || currentApprover.trim().isEmpty() ? "false" : "true";
        delegateExecution.setVariable("moreApprovers", moreApprovers);


    }

    private void moveFirstFutureApproverToCurrentApproverIfNeeded(DelegateExecution delegateExecution)
    {
        // in case we were started with future approvers, but no current approver, set the current approver to the
        // first future approver, and remove the first element from future approvers
        String currentApprover = (String) delegateExecution.getVariable("currentApprover");
        List<String> futureApprovers = (List<String>) delegateExecution.getVariable(TaskConstants.VARIABLE_NAME_BUCKSLIP_FUTURE_APPROVERS);

        if ((currentApprover == null || currentApprover.trim().isEmpty()) &&
                futureApprovers != null && !futureApprovers.isEmpty())
        {
            currentApprover = futureApprovers.get(0);
            futureApprovers = new ArrayList<>(futureApprovers.subList(1, futureApprovers.size()));

            delegateExecution.setVariable("currentApprover", currentApprover);
            delegateExecution.setVariable(TaskConstants.VARIABLE_NAME_BUCKSLIP_FUTURE_APPROVERS, futureApprovers);
        }
    }

    private void copyApproversToFutureApproversIfNeeded(DelegateExecution delegateExecution)
    {
        // in case we have "approvers" but not "futureApprovers", copy the approvers to the future approvers
        List<String> approvers = (List<String>) delegateExecution.getVariable("approvers");
        List<String> future = (List<String>) delegateExecution.getVariable(TaskConstants.VARIABLE_NAME_BUCKSLIP_FUTURE_APPROVERS);
        if (approvers != null && !approvers.isEmpty() && (future == null || future.isEmpty()))
        {
            delegateExecution.setVariable(TaskConstants.VARIABLE_NAME_BUCKSLIP_FUTURE_APPROVERS, approvers);
        }
    }
}