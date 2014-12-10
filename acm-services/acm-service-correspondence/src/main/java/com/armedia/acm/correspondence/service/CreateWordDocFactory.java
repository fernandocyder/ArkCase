package com.armedia.acm.correspondence.service;

import com.armedia.acm.correspondence.model.CorrespondenceType;

/**
 * Created by marjan.stefanoski on 03.12.2014.
 */
public class CreateWordDocFactory {

    public WordDocFromTemplate getWordCreator(CorrespondenceType correspondenceType){
        switch (correspondenceType){
            case CLERANCE_DENIED:
                return new CreateClearanceDenied();
            case CLERANCE_GRANTED:
                return new CreateClearanceGranted();
            case MEDICAL_RELEASE:
                return new CreateMedicalRelease();
            case GENERAL_RELEASE:
                return new CreateMedicalRelease();
            default:
                return null;
        }
    }
}

