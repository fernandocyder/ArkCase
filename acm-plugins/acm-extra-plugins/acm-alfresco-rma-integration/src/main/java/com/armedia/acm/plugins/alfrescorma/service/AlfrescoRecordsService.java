package com.armedia.acm.plugins.alfrescorma.service;

import com.armedia.acm.core.exceptions.AcmListObjectsFailedException;
import com.armedia.acm.core.exceptions.AcmObjectNotFoundException;
import com.armedia.acm.crypto.properties.AcmEncryptablePropertyUtils;
import com.armedia.acm.plugins.alfrescorma.exception.AlfrescoServiceException;
import com.armedia.acm.plugins.alfrescorma.model.AlfrescoRmaPluginConstants;
import com.armedia.acm.plugins.ecm.dao.EcmFileDao;
import com.armedia.acm.plugins.ecm.model.AcmCmisObject;
import com.armedia.acm.plugins.ecm.model.AcmCmisObjectList;
import com.armedia.acm.plugins.ecm.model.AcmContainer;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.model.EcmFileConstants;
import com.armedia.acm.plugins.ecm.service.EcmFileService;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by armdev on 3/27/15.
 */
public class AlfrescoRecordsService implements InitializingBean
{
    private transient final Logger log = LoggerFactory.getLogger(getClass());

    private EcmFileService ecmFileService;
    private Properties alfrescoRmaProperties;
    private Map<String, Object> alfrescoRmaPropertiesMap;
    private EcmFileDao ecmFileDao;
    private AcmEncryptablePropertyUtils encryptablePropertyUtils;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        getEncryptablePropertyUtils().decryptProperties(alfrescoRmaProperties);

        getEncryptablePropertyUtils().decryptProperties(alfrescoRmaPropertiesMap);
    }

    private GetTicketService ticketService;
    private DeclareRecordService declareRecordService;
    private SetRecordMetadataService setRecordMetadataService;
    private FindCategoryFolderService findCategoryFolderService;
    private CreateOrFindRecordFolderService createOrFindRecordFolderService;
    private MoveToRecordFolderService moveToRecordFolderService;
    private CompleteRecordService completeRecordService;

    public void declareAllContainerFilesAsRecords(Authentication auth, AcmContainer container, Date receiveDate, String recordFolderName)
    {
        try
        {
            AcmCmisObjectList files = getEcmFileService().allFilesForContainer(auth, container);
            declareAsRecords(files, container, receiveDate, recordFolderName);
        }
        catch (AcmListObjectsFailedException e)
        {
            log.error("Cannot finish Record Management Strategy for container " + container.getContainerObjectType() + " "
                    + container.getContainerObjectId(), e);
        }
    }

    public void declareAllFilesInFolderAsRecords(AcmCmisObjectList folder, AcmContainer container, Date receiveDate,
            String recordFolderName)
    {
        declareAsRecords(folder, container, receiveDate, recordFolderName);
    }

    public void declareAsRecords(AcmCmisObjectList files, AcmContainer container, Date receiveDate, String recordFolderName)
    {
        String originatorOrg = getAlfrescoRmaProperties().getProperty(AlfrescoRmaPluginConstants.PROPERTY_ORIGINATOR_ORG,
                AlfrescoRmaPluginConstants.DEFAULT_ORIGINATOR_ORG);

        // get the ticket once, to use for all files
        try
        {
            String alfTicket = getTicketService().service(null);

            for (AcmCmisObject file : files.getChildren())
            {
                declareFileAsRecord(container, receiveDate, recordFolderName, originatorOrg, file.getModifier(), alfTicket,
                        file.getCmisObjectId(), file.getStatus(), file.getObjectId());
            }

        }
        catch (AlfrescoServiceException e)
        {
            log.error("Could not declare records: {}", e.getMessage(), e);
        }
    }

    protected void declareFileAsRecord(AcmContainer container, Date receiveDate, String recordFolderName, String originatorOrg,
            String originator, String alfTicket, String cmisObjectId, String objectStatus, Long ecmFileId) throws AlfrescoServiceException
    {
        if (!((EcmFileConstants.RECORD).equals(objectStatus)))
        {
            declareRecord(alfTicket, cmisObjectId);

            writeRecordMetadata(receiveDate, originatorOrg, alfTicket, cmisObjectId, originator);

            CmisObject categoryFolder = findCategoryFolder(container.getObjectType());

            String recordFolderId = createOrFindRecordFolder(recordFolderName, alfTicket, categoryFolder);

            log.debug("recordFolderId: {}", recordFolderId);

            moveToRecordFolder(recordFolderName, alfTicket, cmisObjectId, categoryFolder);

            completeRecord(alfTicket, cmisObjectId);

            setFileStatusAsRecord(ecmFileId);
        }
    }

    protected void completeRecord(String alfTicket, String cmisFileId)
    {
        Map<String, Object> completeRecordContext = new HashMap<>();
        completeRecordContext.put("ecmFileId", cmisFileId);
        completeRecordContext.put("ticket", alfTicket);
    }

    protected void moveToRecordFolder(String recordFolderName, String alfTicket, String cmisFileId, CmisObject categoryFolder)
            throws AlfrescoServiceException
    {
        Map<String, Object> moveToRecordFolderContext = new HashMap<>();
        moveToRecordFolderContext.put("ecmFileId", cmisFileId);
        moveToRecordFolderContext.put("categoryFolderName", categoryFolder.getName());
        moveToRecordFolderContext.put("recordFolderName", recordFolderName);
        moveToRecordFolderContext.put("ticket", alfTicket);
        getMoveToRecordFolderService().service(moveToRecordFolderContext);
    }

    protected String createOrFindRecordFolder(String recordFolderName, String alfTicket, CmisObject categoryFolder)
            throws AlfrescoServiceException
    {
        Map<String, Object> findRecordFolderContext = new HashMap<>();
        findRecordFolderContext.put("categoryFolder", categoryFolder);
        findRecordFolderContext.put("recordFolderName", recordFolderName);
        findRecordFolderContext.put("ticket", alfTicket);
        return getCreateOrFindRecordFolderService().service(findRecordFolderContext);
    }

    protected CmisObject findCategoryFolder(String containerObjectType) throws AlfrescoServiceException
    {
        // find the category folder
        Map<String, Object> findCategoryFolderContext = new HashMap<>();
        findCategoryFolderContext.put("objectType", containerObjectType);
        CmisObject categoryFolder = findCategoryFolderService.service(findCategoryFolderContext);
        return categoryFolder;
    }

    protected void writeRecordMetadata(Date receiveDate, String originatorOrg, String alfTicket, String cmisFileId, String originator)
            throws AlfrescoServiceException
    {
        Map<String, Object> metadataContext = new HashMap<>();
        metadataContext.put("ecmFileId", cmisFileId);
        metadataContext.put("ticket", alfTicket);
        metadataContext.put("publicationDate", new Date());
        metadataContext.put("originator", originator);
        metadataContext.put("originatingOrganization", originatorOrg);
        metadataContext.put("dateReceived", receiveDate);
        getSetRecordMetadataService().service(metadataContext);
    }

    protected void declareRecord(String alfTicket, String cmisFileId) throws AlfrescoServiceException
    {
        Map<String, Object> declareContext = new HashMap<>();
        declareContext.put("ecmFileId", cmisFileId);
        declareContext.put("ticket", alfTicket);
        getDeclareRecordService().service(declareContext);
    }

    public void setFileStatusAsRecord(Long fileId)
    {
        try
        {
            EcmFile ecmFile = getEcmFileService().findById(fileId);
            if (null == ecmFile)
            {
                throw new AcmObjectNotFoundException(EcmFileConstants.OBJECT_FILE_TYPE, fileId, "File not found", null);
            }
            else
            {
                ecmFile.setStatus(EcmFileConstants.RECORD);
                getEcmFileDao().save(ecmFile);
                if (log.isDebugEnabled())
                {
                    log.debug("File with ID : " + ecmFile.getFileId() + " Status is changed to " + EcmFileConstants.RECORD);
                }
            }
        }
        catch (AcmObjectNotFoundException e)
        {
            if (log.isErrorEnabled())
            {
                log.error("File with id: " + fileId + " does not exists - " + e.getMessage());
            }
        }
    }

    public boolean checkIntegrationEnabled(String integrationPointKey)
    {
        String integrationEnabledKey = "alfresco.rma.integration.enabled";

        Properties rmaProps = getAlfrescoRmaProperties();

        return "true".equals(rmaProps.getProperty(integrationEnabledKey, "true"))
                && "true".equals(rmaProps.getProperty(integrationPointKey, "true"));
    }

    public EcmFileService getEcmFileService()
    {
        return ecmFileService;
    }

    public void setEcmFileService(EcmFileService ecmFileService)
    {
        this.ecmFileService = ecmFileService;
    }

    public void setAlfrescoRmaProperties(Properties alfrescoRmaProperties)
    {
        this.alfrescoRmaProperties = alfrescoRmaProperties;

        if (alfrescoRmaProperties != null)
        {
            Map<String, Object> stringObjectMap = new HashMap<>();
            alfrescoRmaProperties.entrySet().stream().filter((entry) -> entry.getKey() != null)
                    .forEach((entry) -> stringObjectMap.put((String) entry.getKey(), entry.getValue()));
            setAlfrescoRmaPropertiesMap(stringObjectMap);
        }
    }

    public Properties getAlfrescoRmaProperties()
    {
        return alfrescoRmaProperties;
    }

    public EcmFileDao getEcmFileDao()
    {
        return ecmFileDao;
    }

    public void setEcmFileDao(EcmFileDao ecmFileDao)
    {
        this.ecmFileDao = ecmFileDao;
    }

    public Map<String, Object> getAlfrescoRmaPropertiesMap()
    {
        return alfrescoRmaPropertiesMap;
    }

    protected void setAlfrescoRmaPropertiesMap(Map<String, Object> alfrescoRmaPropertiesMap)
    {
        this.alfrescoRmaPropertiesMap = alfrescoRmaPropertiesMap;
    }

    public AcmEncryptablePropertyUtils getEncryptablePropertyUtils()
    {
        return encryptablePropertyUtils;
    }

    public void setEncryptablePropertyUtils(AcmEncryptablePropertyUtils encryptablePropertyUtils)
    {
        this.encryptablePropertyUtils = encryptablePropertyUtils;
    }

    public GetTicketService getTicketService()
    {
        return ticketService;
    }

    public void setTicketService(GetTicketService ticketService)
    {
        this.ticketService = ticketService;
    }

    public DeclareRecordService getDeclareRecordService()
    {
        return declareRecordService;
    }

    public void setDeclareRecordService(DeclareRecordService declareRecordService)
    {
        this.declareRecordService = declareRecordService;
    }

    public SetRecordMetadataService getSetRecordMetadataService()
    {
        return setRecordMetadataService;
    }

    public void setSetRecordMetadataService(SetRecordMetadataService setRecordMetadataService)
    {
        this.setRecordMetadataService = setRecordMetadataService;
    }

    public FindCategoryFolderService getFindCategoryFolderService()
    {
        return findCategoryFolderService;
    }

    public void setFindCategoryFolderService(FindCategoryFolderService findCategoryFolderService)
    {
        this.findCategoryFolderService = findCategoryFolderService;
    }

    public CreateOrFindRecordFolderService getCreateOrFindRecordFolderService()
    {
        return createOrFindRecordFolderService;
    }

    public void setCreateOrFindRecordFolderService(CreateOrFindRecordFolderService createOrFindRecordFolderService)
    {
        this.createOrFindRecordFolderService = createOrFindRecordFolderService;
    }

    public MoveToRecordFolderService getMoveToRecordFolderService()
    {
        return moveToRecordFolderService;
    }

    public void setMoveToRecordFolderService(MoveToRecordFolderService moveToRecordFolderService)
    {
        this.moveToRecordFolderService = moveToRecordFolderService;
    }

    public CompleteRecordService getCompleteRecordService()
    {
        return completeRecordService;
    }

    public void setCompleteRecordService(CompleteRecordService completeRecordService)
    {
        this.completeRecordService = completeRecordService;
    }
}
