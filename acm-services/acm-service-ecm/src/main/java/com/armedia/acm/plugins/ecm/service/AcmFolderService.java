package com.armedia.acm.plugins.ecm.service;

import com.armedia.acm.core.exceptions.AcmCreateObjectFailedException;
import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.plugins.ecm.model.AcmFolder;

/**
 * Created by marjan.stefanoski on 03.04.2015.
 */
public interface AcmFolderService {

    public AcmFolder addNewFolder(String parentFolderPath, String folderName) throws AcmCreateObjectFailedException;
    public void renameFolder(String cmisObjectId, String newFolderName) throws AcmUserActionFailedException;

}
