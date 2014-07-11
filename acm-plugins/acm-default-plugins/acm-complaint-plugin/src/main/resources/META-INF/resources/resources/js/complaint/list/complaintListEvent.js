/**
 * ComplaintList.Event
 *
 * event handlers for objects
 *
 * @author jwu
 */
ComplaintList.Event = {
    initialize : function() {
    }

    ,onClickLnkListItemImage : function(e) {
        var complaintId = ComplaintList.Object.getHiddenComplaintId(e);
        if (Complaint.getComplaintId() == complaintId) {
            return;
        } else {
            Complaint.setComplaintId(complaintId);
        }

        this.doClickLnkListItem();
    }
    ,onClickLnkListItem : function(e) {
        var complaintId = ComplaintList.Object.getHiddenComplaintId(e);
        if (Complaint.getComplaintId() == complaintId) {
            return;
        } else {
            Complaint.setComplaintId(complaintId);
        }

        this.doClickLnkListItem();
    }
    ,doClickLnkListItem: function() {
        var complaintId = Complaint.getComplaintId();
        ComplaintList.Service.retrieveDetail(complaintId);

        var c = ComplaintList.findComplaint(complaintId);
        if (null != c) {
            ComplaintList.Object.updateDetail(c);
            Complaint.setComplaintId(complaintId);
            ComplaintList.Object.hiliteSelectedItem(complaintId);
        }
    }

    ,onPostInit: function() {
        if (ComplaintList.isSingleObject()) {
            var complaintId = Complaint.getComplaintId();
            ComplaintList.Service.retrieveDetail(complaintId);
        } else {
            ComplaintList.Service.listComplaint();
        }

        Acm.keepTrying(ComplaintList.Event._tryInitAssignee, 8, 200);
        Acm.keepTrying(ComplaintList.Event._tryInitPriority, 8, 200);
        Acm.keepTrying(ComplaintList.Event._tryInitComplaintType, 8, 200);
    }

    ,_tryInitAssignee: function() {
        var data = Acm.Object.getApprovers();
        if (Acm.isNotEmpty(data)) {
            ComplaintList.Object.initAssignee(data);
            return true;
        } else {
            return false;
        }
    }
    ,_tryInitPriority: function() {
        var data = Acm.Object.getPriorities();
        if (Acm.isNotEmpty(data)) {
            ComplaintList.Object.initPriority(data);
            return true;
        } else {
            return false;
        }
    }
    ,_tryInitComplaintType: function() {
        var data = Acm.Object.getComplaintTypes();
        if (Acm.isNotEmpty(data)) {
            ComplaintList.Object.initComplaintType(data);
            return true;
        } else {
            return false;
        }
    }

};