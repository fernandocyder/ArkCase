package com.armedia.acm.services.sequence.listener;

/*-
 * #%L
 * ACM Service: Sequence Manager
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.armedia.acm.data.AcmDatabaseRollbackChangesEvent;
import com.armedia.acm.data.AcmEntity;
import com.armedia.acm.data.AcmObjectRollbacklist;
import com.armedia.acm.services.sequence.annotation.AcmSequenceAnnotationReader;
import com.armedia.acm.services.sequence.service.AcmSequenceService;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author sasko.tanaskoski
 *
 */
public class AcmSequenceDatabaseRollbackChangesHandler implements ApplicationListener<AcmDatabaseRollbackChangesEvent>
{
    private transient Logger log = LoggerFactory.getLogger(getClass());

    private AcmSequenceAnnotationReader acmSequenceAnnotationReader;

    private AcmSequenceService sequenceService;

    @Override
    public void onApplicationEvent(AcmDatabaseRollbackChangesEvent acmDatabaseRollbackChangesEvent)
    {
        AcmObjectRollbacklist changes = acmDatabaseRollbackChangesEvent.getObjectRollbackChangelist();

        changes.getPreInsertObjects().stream().forEach(o -> updateSequenceAsUnused(o));
    }

    public void updateSequenceAsUnused(Object object)
    {
        if (object instanceof AcmEntity)
        {
            List<Field> annotatedFields = getAcmSequenceAnnotationReader().getAnnotatedFields(object.getClass());
            for (Field annotatedField : annotatedFields)
            {
                if (annotatedField != null)
                {
                    try
                    {
                        String sequenceValue = PropertyUtils.getProperty(object, annotatedField.getName()).toString();
                        getSequenceService().updateSequenceRegistryAsUnused(sequenceValue);
                    }
                    catch (Exception e)
                    {
                        log.error("Error updating sequence as unused on field [{}], reason [{}]", annotatedField.getName(),
                                e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * @return the acmSequenceAnnotationReader
     */
    public AcmSequenceAnnotationReader getAcmSequenceAnnotationReader()
    {
        return acmSequenceAnnotationReader;
    }

    /**
     * @param acmSequenceAnnotationReader
     *            the acmSequenceAnnotationReader to set
     */
    public void setAcmSequenceAnnotationReader(AcmSequenceAnnotationReader acmSequenceAnnotationReader)
    {
        this.acmSequenceAnnotationReader = acmSequenceAnnotationReader;
    }

    /**
     * @return the sequenceService
     */
    public AcmSequenceService getSequenceService()
    {
        return sequenceService;
    }

    /**
     * @param sequenceService
     *            the sequenceService to set
     */
    public void setSequenceService(AcmSequenceService sequenceService)
    {
        this.sequenceService = sequenceService;
    }

}
