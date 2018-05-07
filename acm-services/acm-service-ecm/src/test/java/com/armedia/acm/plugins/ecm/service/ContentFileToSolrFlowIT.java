package com.armedia.acm.plugins.ecm.service;

import static org.junit.Assert.assertNotNull;

import com.armedia.acm.muletools.mulecontextmanager.MuleContextManager;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.services.search.model.solr.SolrContentDocument;
import com.armedia.acm.services.search.service.SendDocumentsToSolr;
import com.armedia.acm.web.api.MDCConstants;

import org.apache.chemistry.opencmis.client.api.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/spring/spring-library-add-file-mule.xml",
        "/spring/spring-mule-activemq.xml",
        "/spring/spring-library-property-file-manager.xml",
        "/spring/spring-library-acm-encryption.xml",
        "/spring/spring-library-search.xml",
        "/spring/spring-library-object-converter.xml",
        "/spring/spring-library-data-source.xml",
        "/spring/spring-library-ecm-file.xml",
        "/spring/spring-library-ecm-tika.xml",
        "/spring/spring-library-object-lock.xml",
        "/spring/spring-library-user-service.xml",
        "/spring/spring-library-data-access-control.xml",
        "/spring/spring-library-drools-rule-monitor.xml",
        "/spring/spring-library-activiti-configuration.xml",
        "/spring/spring-library-particpants.xml",
        "/spring/spring-library-context-holder.xml"
})
public class ContentFileToSolrFlowIT
{
    @Autowired
    private MuleContextManager muleContextManager;

    @Autowired
    private SendDocumentsToSolr sendDocumentsToSolr;

    private transient final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void sendFile() throws Exception
    {
        MDC.put(MDCConstants.EVENT_MDC_REQUEST_ALFRESCO_USER_ID_KEY, "admin");
        MDC.put(MDCConstants.EVENT_MDC_REQUEST_ID_KEY, UUID.randomUUID().toString());

        String testPath = "/acm/test/folder";
        Map<String, Object> folderMessageProperties = new HashMap<>();
        folderMessageProperties.put("configRef", muleContextManager.getMuleContext().getRegistry().lookupObject("alfresco"));
        MuleMessage folderMessage = muleContextManager.send("vm://getTestFolderId.in", testPath, folderMessageProperties);
        String folderId = folderMessage.getPayloadAsString();

        Resource uploadFile = new ClassPathResource("/spring/spring-library-ecm-plugin-test-mule.xml");
        InputStream is = uploadFile.getInputStream();

        EcmFile ecmFile = new EcmFile();

        ecmFile.setFileName("spring-library-ecm-plugin-test-mule.xml-" + System.currentTimeMillis());
        ecmFile.setFileActiveVersionMimeType("text/plain");

        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("cmisFolderId", folderId);
        messageProperties.put("inputStream", is);

        messageProperties.put("configRef", muleContextManager.getMuleContext().getRegistry().lookupObject("alfresco"));
        messageProperties.put("versioningState", "MAJOR");
        MuleMessage message = muleContextManager.send("vm://addFile.in", ecmFile, messageProperties);

        assertNotNull(message);

        Document found = message.getPayload(Document.class);
        assertNotNull(found.getVersionSeriesId());
        assertNotNull(found.getContentStreamMimeType());
        assertNotNull(found.getVersionLabel());

        log.debug("doc id: {}", found.getVersionSeriesId());

        SolrContentDocument solrContentDocument = new SolrContentDocument();
        solrContentDocument.setCmis_version_series_id_s(found.getVersionSeriesId());
        solrContentDocument.setAdditionalProperty("cmis_repository_id_s", "alfresco");
        solrContentDocument.setName("/spring/spring-library-ecm-plugin-test-mule" + System.currentTimeMillis() + ".xml");

        sendDocumentsToSolr.sendSolrContentFileIndexDocuments(Collections.singletonList(solrContentDocument));

        // wait for JMS to do its thing
        Thread.sleep(30000);

    }
}
