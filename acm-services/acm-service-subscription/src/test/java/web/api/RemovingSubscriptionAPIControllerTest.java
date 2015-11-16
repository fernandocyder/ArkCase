package web.api;

import com.armedia.acm.pluginmanager.model.AcmPlugin;
import com.armedia.acm.services.subscription.dao.SubscriptionDao;
import com.armedia.acm.services.subscription.model.AcmSubscription;
import com.armedia.acm.services.subscription.service.SubscriptionEventPublisher;
import com.armedia.acm.services.subscription.web.api.RemovingSubscriptionAPIController;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

/**
 * Created by marjan.stefanoski on 12.02.2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/spring-web-acm-web.xml",
        "classpath:/spring/spring-library-subscription-web-api-test.xml"
})
public class RemovingSubscriptionAPIControllerTest extends EasyMockSupport {
    private MockMvc mockMvc;
    private Authentication mockAuthentication;
    private RemovingSubscriptionAPIController mockRemovingSubscriptionAPIController;
    private SubscriptionDao mockSubscriptionDao;
    private AcmPlugin mockSubscriptionPlugin;
    private SubscriptionEventPublisher mockSubscriptionEventPublisher;
    private MockHttpSession mockHttpSession;

    @Autowired
    private ExceptionHandlerExceptionResolver exceptionResolver;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Before
    public void setUp() throws Exception {

        mockRemovingSubscriptionAPIController = new RemovingSubscriptionAPIController();

        mockSubscriptionDao = createMock(SubscriptionDao.class);
        mockSubscriptionPlugin = createMock(AcmPlugin.class);
        mockSubscriptionEventPublisher = createMock(SubscriptionEventPublisher.class);
        mockSubscriptionPlugin = createMock(AcmPlugin.class);

        mockHttpSession = new MockHttpSession();

        mockRemovingSubscriptionAPIController.setSubscriptionDao(mockSubscriptionDao);
        mockRemovingSubscriptionAPIController.setSubscriptionPlugin(mockSubscriptionPlugin);
        mockRemovingSubscriptionAPIController.setSubscriptionEventPublisher(mockSubscriptionEventPublisher);

        mockMvc = MockMvcBuilders.standaloneSetup(mockRemovingSubscriptionAPIController).setHandlerExceptionResolvers(exceptionResolver).build();
        mockAuthentication = createMock(Authentication.class);
    }

    @Test
    public void removeSubscription() throws Exception {

        String objectType = "NEW_OBJECT_TYPE";
        Long objectId = 100L;
        String userId = "user-acm";

        Map<String,Object> prop =  new HashMap<>();
        prop.put("subscription.removed.successful","SUCCESS");

        expect(mockSubscriptionPlugin.getPluginProperties()).andReturn(prop).once();
        expect(mockSubscriptionDao.deleteSubscription(userId,objectId,objectType)).andReturn(1);

        mockSubscriptionEventPublisher.publishSubscriptionDeletedEvent(userId,objectId,objectType,true);

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn(userId);

        replayAll();
        MvcResult result = mockMvc.perform(
                delete("/api/latest/service/subscription/{userId}/{objType}/{objId}", userId, objectType,objectId )
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .session(mockHttpSession)
                        .principal(mockAuthentication))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }


    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public Authentication getMockAuthentication() {
        return mockAuthentication;
    }

    public void setMockAuthentication(Authentication mockAuthentication) {
        this.mockAuthentication = mockAuthentication;
    }

    public ExceptionHandlerExceptionResolver getExceptionResolver() {
        return exceptionResolver;
    }

    public void setExceptionResolver(ExceptionHandlerExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
    }
}