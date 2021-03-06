package com.armedia.acm.services.transcribe.provider.aws.service;

/*-
 * #%L
 * ACM Service: Transcribe
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.LimitExceededException;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.Transcript;
import com.amazonaws.services.transcribe.model.TranscriptionJob;
import com.amazonaws.services.transcribe.model.TranscriptionJobStatus;
import com.armedia.acm.files.propertymanager.PropertyFileManager;
import com.armedia.acm.muletools.mulecontextmanager.MuleContextManager;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.model.EcmFileVersion;
import com.armedia.acm.plugins.ecm.service.EcmFileTransaction;
import com.armedia.acm.services.transcribe.exception.CreateTranscribeException;
import com.armedia.acm.services.transcribe.model.Transcribe;
import com.armedia.acm.services.transcribe.model.TranscribeConfiguration;
import com.armedia.acm.services.transcribe.model.TranscribeType;
import com.armedia.acm.services.transcribe.service.TranscribeConfigurationPropertiesService;
import com.armedia.acm.services.transcribe.service.TranscribeEventPublisher;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mule.api.MuleMessage;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Riste Tutureski <riste.tutureski@armedia.com> on 03/13/2018
 */
@RunWith(MockitoJUnitRunner.class)
public class AWSTranscribeServiceTest
{
    private AWSTranscribeService awsTranscribeService;

    private AWSTranscribeConfigurationPropertiesService awsTranscribeConfigurationPropertiesService;

    @Mock
    private PropertyFileManager propertyFileManager;

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private AmazonTranscribe transcribeClient;

    @Mock
    private EcmFileTransaction ecmFileTransaction;

    @Mock
    private InputStream inputStream;

    @Mock
    private TranscribeEventPublisher transcribeEventPublisher;

    @Mock
    private MuleContextManager muleContextManager;

    @Mock
    private MuleMessage muleMessage;

    @Mock
    private TranscribeConfigurationPropertiesService transcribeConfigurationPropertiesService;

    @Before
    public void setUp()
    {
        awsTranscribeConfigurationPropertiesService = new AWSTranscribeConfigurationPropertiesService();
        awsTranscribeConfigurationPropertiesService.setPropertyFileManager(propertyFileManager);

        awsTranscribeService = new AWSTranscribeService();
        awsTranscribeService.setS3Client(s3Client);
        awsTranscribeService.setTranscribeClient(transcribeClient);
        awsTranscribeService.setEcmFileTransaction(ecmFileTransaction);
        awsTranscribeService.setAwsTranscribeConfigurationPropertiesService(awsTranscribeConfigurationPropertiesService);
        awsTranscribeService.setTranscribeEventPublisher(transcribeEventPublisher);
        awsTranscribeService.setMuleContextManager(muleContextManager);
        awsTranscribeService.setTranscribeConfigurationPropertiesService(transcribeConfigurationPropertiesService);
    }

    @Test
    public void create() throws Exception
    {
        EcmFile file = new EcmFile();
        file.setFileId(103l);

        EcmFileVersion version = new EcmFileVersion();
        version.setId(101l);
        version.setDurationSeconds(200d);
        version.setVersionMimeType("video/mp4");
        version.setVersionFileNameExtension(".mp4");
        version.setFileSizeBytes(1000l);
        version.setFile(file);

        Transcribe transcribe = new Transcribe();
        transcribe.setId(102l);
        transcribe.setType(TranscribeType.AUTOMATIC.toString());
        transcribe.setRemoteId("remote-id");
        transcribe.setMediaEcmFileVersion(version);

        Map<String, Object> properties = new HashMap<>();
        properties.put("aws.bucket", "bucket");
        properties.put("aws.region", "region");
        properties.put("aws.host", "host");
        properties.put("aws.profile", "profile");

        when(propertyFileManager.loadMultiple(any(), any())).thenReturn(properties);
        when(s3Client.doesObjectExist((String) properties.get("aws.bucket"),
                transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension())).thenReturn(false);
        when(ecmFileTransaction.downloadFileTransactionAsInputStream(file)).thenReturn(inputStream);
        when(s3Client.putObject(eq((String) properties.get("aws.bucket")),
                eq(transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension()), eq(inputStream), any()))
                        .thenReturn(new PutObjectResult());
        when(transcribeClient.startTranscriptionJob(any())).thenReturn(new StartTranscriptionJobResult());

        awsTranscribeService.create(transcribe);

        verify(propertyFileManager, times(3)).loadMultiple(any(), any());
        verify(s3Client).doesObjectExist((String) properties.get("aws.bucket"),
                transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension());
        verify(ecmFileTransaction).downloadFileTransactionAsInputStream(file);
        verify(s3Client).putObject(eq((String) properties.get("aws.bucket")),
                eq(transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension()), eq(inputStream), any());
        verify(transcribeClient).startTranscriptionJob(any());
    }

    @Test
    public void create_Transcribe_Configuration_Not_Retrieved() throws Exception
    {
        String expectedErrorMessage = "Transcribe failed to create on Amazon. REASON=[Failed to retrieve Configuration.]";
        try
        {
            awsTranscribeService.create(null);
        }
        catch (Exception e)
        {
            assertTrue(e instanceof CreateTranscribeException);
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void create_Amazon_Object_Already_Exist() throws Exception
    {
        EcmFile file = new EcmFile();
        file.setFileId(103l);

        EcmFileVersion version = new EcmFileVersion();
        version.setId(101l);
        version.setDurationSeconds(200d);
        version.setVersionMimeType("video/mp4");
        version.setVersionFileNameExtension(".mp4");
        version.setFileSizeBytes(1000l);
        version.setFile(file);

        Transcribe transcribe = new Transcribe();
        transcribe.setId(102l);
        transcribe.setType(TranscribeType.AUTOMATIC.toString());
        transcribe.setRemoteId("remote-id");
        transcribe.setMediaEcmFileVersion(version);

        Map<String, Object> properties = new HashMap<>();
        properties.put("aws.bucket", "bucket");
        properties.put("aws.region", "region");
        properties.put("aws.host", "host");
        properties.put("aws.profile", "profile");

        String key = transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension();
        String expectedErrorMessage = "The file with KEY=[" + key + "] already exist on Amazon.";

        when(propertyFileManager.loadMultiple(any(), any())).thenReturn(properties);
        when(s3Client.doesObjectExist((String) properties.get("aws.bucket"),
                transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension())).thenReturn(true);

        try
        {
            awsTranscribeService.create(transcribe);
        }
        catch (Exception e)
        {
            verify(propertyFileManager).loadMultiple(any(), any());
            verify(s3Client).doesObjectExist((String) properties.get("aws.bucket"),
                    transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension());

            assertTrue(e instanceof CreateTranscribeException);
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void create_Error_While_Upload() throws Exception
    {
        EcmFile file = new EcmFile();
        file.setFileId(103l);

        EcmFileVersion version = new EcmFileVersion();
        version.setId(101l);
        version.setDurationSeconds(200d);
        version.setVersionMimeType("video/mp4");
        version.setVersionFileNameExtension(".mp4");
        version.setFileSizeBytes(1000l);
        version.setFile(file);

        Transcribe transcribe = new Transcribe();
        transcribe.setId(102l);
        transcribe.setType(TranscribeType.AUTOMATIC.toString());
        transcribe.setRemoteId("remote-id");
        transcribe.setMediaEcmFileVersion(version);

        Map<String, Object> properties = new HashMap<>();
        properties.put("aws.bucket", "bucket");
        properties.put("aws.region", "region");
        properties.put("aws.host", "host");
        properties.put("aws.profile", "profile");

        String expectedErrorMessage = "Unable to upload media file to Amazon. REASON=[error (Service: null; Status Code: 0; Error Code: null; Request ID: null)].";

        when(propertyFileManager.loadMultiple(any(), any())).thenReturn(properties);
        when(s3Client.doesObjectExist((String) properties.get("aws.bucket"),
                transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension())).thenReturn(false);
        when(ecmFileTransaction.downloadFileTransactionAsInputStream(file)).thenReturn(inputStream);
        when(s3Client.putObject(eq((String) properties.get("aws.bucket")),
                eq(transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension()), eq(inputStream), any()))
                        .thenThrow(new AmazonServiceException("error"));

        try
        {
            awsTranscribeService.create(transcribe);
        }
        catch (Exception e)
        {
            verify(propertyFileManager, times(2)).loadMultiple(any(), any());
            verify(s3Client).doesObjectExist((String) properties.get("aws.bucket"),
                    transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension());
            verify(ecmFileTransaction).downloadFileTransactionAsInputStream(file);
            verify(s3Client).putObject(eq((String) properties.get("aws.bucket")),
                    eq(transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension()), eq(inputStream),
                    any());

            assertTrue(e instanceof CreateTranscribeException);
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void create_Error_Start_Transcribe_Job() throws Exception
    {
        EcmFile file = new EcmFile();
        file.setFileId(103l);

        EcmFileVersion version = new EcmFileVersion();
        version.setId(101l);
        version.setDurationSeconds(200d);
        version.setVersionMimeType("video/mp4");
        version.setVersionFileNameExtension(".mp4");
        version.setFileSizeBytes(1000l);
        version.setFile(file);

        Transcribe transcribe = new Transcribe();
        transcribe.setId(102l);
        transcribe.setType(TranscribeType.AUTOMATIC.toString());
        transcribe.setRemoteId("remote-id");
        transcribe.setMediaEcmFileVersion(version);

        Map<String, Object> properties = new HashMap<>();
        properties.put("aws.bucket", "bucket");
        properties.put("aws.region", "region");
        properties.put("aws.host", "host");
        properties.put("aws.profile", "profile");

        String expectedErrorMessage = "Unable to start transcribe job on Amazon. REASON=[error (Service: null; Status Code: 0; Error Code: null; Request ID: null)]";

        when(propertyFileManager.loadMultiple(any(), any())).thenReturn(properties);
        when(s3Client.doesObjectExist((String) properties.get("aws.bucket"),
                transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension())).thenReturn(false);
        when(ecmFileTransaction.downloadFileTransactionAsInputStream(file)).thenReturn(inputStream);
        when(s3Client.putObject(eq((String) properties.get("aws.bucket")),
                eq(transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension()), eq(inputStream), any()))
                        .thenReturn(new PutObjectResult());
        when(transcribeClient.startTranscriptionJob(any())).thenThrow(new LimitExceededException("error"));

        try
        {
            awsTranscribeService.create(transcribe);
        }
        catch (Exception e)
        {
            verify(propertyFileManager, times(3)).loadMultiple(any(), any());
            verify(s3Client).doesObjectExist((String) properties.get("aws.bucket"),
                    transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension());
            verify(ecmFileTransaction).downloadFileTransactionAsInputStream(file);
            verify(s3Client).putObject(eq((String) properties.get("aws.bucket")),
                    eq(transcribe.getRemoteId() + transcribe.getMediaEcmFileVersion().getVersionFileNameExtension()), eq(inputStream),
                    any());
            verify(transcribeClient).startTranscriptionJob(any());

            assertTrue(e instanceof CreateTranscribeException);
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void get() throws Exception
    {
        String remoteId = "transcribe-remote-id";
        String jsonString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("aws/output/asrOutput.json"));

        TranscribeConfiguration configuration = new TranscribeConfiguration();
        configuration.setWordCountPerItem(20);
        configuration.setSilentBetweenWords(new BigDecimal("2"));

        Transcript transcript = new Transcript();
        transcript.setTranscriptFileUri("https://www.amazon.test.example.com");

        TranscriptionJob transcriptionJob = new TranscriptionJob();
        transcriptionJob.setTranscriptionJobStatus(TranscriptionJobStatus.COMPLETED.toString());
        transcriptionJob.setTranscript(transcript);

        GetTranscriptionJobResult getTranscriptionJobResult = new GetTranscriptionJobResult();
        getTranscriptionJobResult.setTranscriptionJob(transcriptionJob);

        when(transcribeClient.getTranscriptionJob(any())).thenReturn(getTranscriptionJobResult);
        when(muleContextManager.send("vm://getProviderTranscribe.in", "www.amazon.test.example.com")).thenReturn(muleMessage);
        when(muleMessage.getInboundProperty("getProviderTranscribeException")).thenReturn(null);
        when(muleMessage.getPayloadAsString()).thenReturn(jsonString);
        when(transcribeConfigurationPropertiesService.get()).thenReturn(configuration);

        Transcribe transcribe = awsTranscribeService.get(remoteId);

        verify(transcribeClient).getTranscriptionJob(any());
        verify(muleContextManager).send("vm://getProviderTranscribe.in", "www.amazon.test.example.com");
        verify(muleMessage).getInboundProperty("getProviderTranscribeException");
        verify(muleMessage).getPayloadAsString();
        verify(transcribeConfigurationPropertiesService, times(346)).get();

        assertNotNull(transcribe);
        assertNotNull(transcribe.getTranscribeItems());
        assertEquals(22, transcribe.getTranscribeItems().size());
        assertEquals(new BigDecimal("1.390"), transcribe.getTranscribeItems().get(0).getStartTime());
        assertEquals(new BigDecimal("7.720"), transcribe.getTranscribeItems().get(0).getEndTime());
        assertEquals(98, transcribe.getTranscribeItems().get(0).getConfidence());
        assertEquals("I've often said that i wish people could realize all their dreams and wealth fame and so that they could",
                transcribe.getTranscribeItems().get(0).getText());
        assertEquals(new BigDecimal("169.120"), transcribe.getTranscribeItems().get(21).getStartTime());
        assertEquals(new BigDecimal("177.730"), transcribe.getTranscribeItems().get(21).getEndTime());
        assertEquals(61, transcribe.getTranscribeItems().get(21).getConfidence());
        assertEquals("Wait", transcribe.getTranscribeItems().get(21).getText());

        assertEquals(configuration.getWordCountPerItem(), transcribe.getTranscribeItems().get(0).getText().split(" ").length);

        // There is a silent between words, new item is created
        assertEquals(13, transcribe.getTranscribeItems().get(1).getText().split(" ").length);

        // ... Also at the end there is silent between words. New item is created
        assertEquals(1, transcribe.getTranscribeItems().get(21).getText().split(" ").length);
    }
}
