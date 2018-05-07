package com.armedia.acm.services.search.service;

import com.armedia.acm.objectonverter.ObjectConverter;
import com.armedia.acm.services.search.model.solr.SolrAdvancedSearchDocument;
import com.armedia.acm.services.search.model.solr.SolrBaseDocument;
import com.armedia.acm.services.search.model.solr.SolrContentDocument;
import com.armedia.acm.services.search.model.solr.SolrDeleteDocumentByIdRequest;
import com.armedia.acm.services.search.model.solr.SolrDocument;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by armdev on 10/21/14.
 */
public class SendDocumentsToSolr implements InitializingBean
{
    private ObjectConverter objectConverter;

    private ConnectionFactory jmsConnectionFactory;
    private JmsTemplate jmsTemplate;

    private transient final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void afterPropertiesSet() throws Exception
    {
        jmsTemplate = new JmsTemplate(getJmsConnectionFactory());
    }

    // this method is used from Mule, do not delete it!
    public String asJsonArray(SolrBaseDocument document) throws JsonProcessingException
    {
        log.trace("Converting a document to a JSON array");
        List<SolrBaseDocument> docs = Collections.singletonList(document);
        String json = objectConverter.getJsonMarshaller().marshal(docs);
        return json;
    }

    public void sendSolrAdvancedSearchDocuments(List<SolrAdvancedSearchDocument> solrDocuments)
    {
        sendToJmsQueue(solrDocuments, "solrAdvancedSearch.in");
    }

    public void sendSolrQuickSearchDocuments(List<SolrDocument> solrDocuments)
    {
        sendToJmsQueue(solrDocuments, "solrQuickSearch.in");
    }

    public void sendSolrContentFileIndexDocuments(List<SolrContentDocument> solrDocuments)
    {
        if (solrDocuments != null)
        {
            for (SolrContentDocument doc : solrDocuments)
            {
                log.debug("sending to solrContentFile.in");
                sendToJmsQueue(doc, "solrContentFile.in");
            }

        }
    }

    public void sendSolrContentFileIndexDeletes(List<SolrDeleteDocumentByIdRequest> deletes)
    {
        // send separate requests, in case any of them fail, e.g. maybe a doc with this id already is not in the
        // index.
        if (deletes != null)
        {
            for (SolrDeleteDocumentByIdRequest doc : deletes)
            {
                sendToJmsQueue(doc, "solrAdvancedSearch.in");
            }
        }
    }

    public void sendSolrQuickSearchDeletes(List<SolrDeleteDocumentByIdRequest> deletes)
    {
        // send separate requests, in case any of them fail, e.g. maybe a doc with this id already is not in the
        // index.
        if (deletes != null)
        {
            for (SolrDeleteDocumentByIdRequest doc : deletes)
            {
                sendToJmsQueue(doc, "solrQuickSearch.in");
            }
        }
    }

    public void sendSolrAdvancedSearchDeletes(List<SolrDeleteDocumentByIdRequest> deletes)
    {
        log.debug("Received [{}] to be deleted.", deletes.size());
        // send separate requests, in case any of them fail, e.g. maybe a doc with this id already is not in the
        // index.
        if (deletes != null)
        {
            for (SolrDeleteDocumentByIdRequest doc : deletes)
            {
                sendToJmsQueue(doc, "solrAdvancedSearch.in");
            }
        }
    }

    public void sendSolrDocuments(String queueName, String json)
    {
        getJmsTemplate().convertAndSend(queueName, json);
    }

    private void sendToJmsQueue(SolrDeleteDocumentByIdRequest solrDocument, String queueName)
    {
        try
        {
            String json = objectConverter.getJsonMarshaller().marshal(solrDocument);

            log.debug("Sending JSON to SOLR with hash {}", json.hashCode());

            getJmsTemplate().convertAndSend(queueName, json);
            log.debug("Sent JSON to SOLR with hash {}", json.hashCode());

            log.trace("Returning JSON: {}", json);

        }
        catch (JmsException e)
        {
            log.error("Could not send document to SOLR: {}", e.getMessage(), e);
        }
    }

    private void sendToJmsQueue(SolrContentDocument solrDocument, String queueName)
    {
        try
        {
            log.trace("Sending POJO to SOLR: {}", solrDocument);

            String json = objectConverter.getJsonMarshaller().marshal(solrDocument);

            log.debug("Sending a doc to Solr with hash {}", solrDocument.hashCode());
            getJmsTemplate().convertAndSend(queueName, json);
            log.debug("Sent a doc to Solr with hash {}", solrDocument.hashCode());
        }
        catch (JmsException e)
        {
            log.error("Could not send document to SOLR: " + e.getMessage(), e);
        }
    }

    private void sendToJmsQueue(List<? extends SolrBaseDocument> solrDocuments, String queueName)
    {
        try
        {
            String json = objectConverter.getJsonMarshaller().marshal(solrDocuments);

            log.debug("Sending json to Solr via JMS with hash {}", json.hashCode());
            getJmsTemplate().convertAndSend(queueName, json);
            log.debug("Sent json to Solr via JMS with hash {}", json.hashCode());

            log.trace("Returning JSON: {}", json);

        }
        catch (JmsException e)
        {
            log.error("Could not send document to SOLR: " + e.getMessage(), e);
        }
    }

    public ObjectConverter getObjectConverter()
    {
        return objectConverter;
    }

    public void setObjectConverter(ObjectConverter objectConverter)
    {
        this.objectConverter = objectConverter;
    }

    public ConnectionFactory getJmsConnectionFactory()
    {
        return jmsConnectionFactory;
    }

    public void setJmsConnectionFactory(ConnectionFactory jmsConnectionFactory)
    {
        this.jmsConnectionFactory = jmsConnectionFactory;
    }

    public JmsTemplate getJmsTemplate()
    {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate)
    {
        this.jmsTemplate = jmsTemplate;
    }

}
