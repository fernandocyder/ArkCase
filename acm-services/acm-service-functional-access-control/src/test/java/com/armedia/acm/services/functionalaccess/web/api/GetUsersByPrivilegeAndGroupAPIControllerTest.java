package com.armedia.acm.services.functionalaccess.web.api;

import com.armedia.acm.pluginmanager.service.AcmPluginManager;
import com.armedia.acm.services.functionalaccess.service.FunctionalAccessService;
import com.armedia.acm.services.users.dao.group.AcmGroupDao;
import com.armedia.acm.services.users.model.AcmUser;
import com.armedia.acm.services.users.model.group.AcmGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * @author riste.tutureski
 */
public class GetUsersByPrivilegeAndGroupAPIControllerTest extends EasyMockSupport
{

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private MockMvc mockMvc;
    private GetUsersByPrivilegeAndGroupAPIController unit;
    private Authentication mockAuthentication;
    private FunctionalAccessService mockFunctionalAccessService;
    private AcmPluginManager mockPluginManager;
    private AcmGroupDao mockAcmGroupDao;

    @Autowired
    private ExceptionHandlerExceptionResolver exceptionResolver;

    @Before
    public void setUp() throws Exception
    {
        unit = new GetUsersByPrivilegeAndGroupAPIController();
        mockMvc = MockMvcBuilders.standaloneSetup(unit).setHandlerExceptionResolvers(exceptionResolver).build();
        mockAuthentication = createMock(Authentication.class);
        mockPluginManager = createMock(AcmPluginManager.class);
        mockAcmGroupDao = createMock(AcmGroupDao.class);

        mockFunctionalAccessService = createMock(FunctionalAccessService.class);
        unit.setFunctionalAccessService(mockFunctionalAccessService);
        unit.setPluginManager(mockPluginManager);
    }

    @Test
    public void usersByPrivilegeTest() throws Exception
    {

        String privilege = "acm-privilege";
        String role1 = "ROLE_ADMINISTRATOR";
        String role2 = "ROLE_INVESTIGATOR_SUPERVISOR";

        // Group 1
        Set<AcmUser> membersGroup1 = new HashSet<>();

        AcmUser user1 = new AcmUser();
        user1.setUserId("user1");

        AcmUser user2 = new AcmUser();
        user2.setUserId("user2");

        membersGroup1.addAll(Arrays.asList(user1, user2));

        AcmGroup group1 = new AcmGroup();
        group1.setName("acm_administrator_dev");
        group1.setMembers(membersGroup1);

        // Group 2
        Set<AcmUser> membersGroup2 = new HashSet<>();

        AcmUser user3 = new AcmUser();
        user3.setUserId("user3");

        AcmUser user4 = new AcmUser();
        user4.setUserId("user4");

        membersGroup2.addAll(Arrays.asList(user3, user4));

        AcmGroup group2 = new AcmGroup();
        group2.setName("acm_supervisor_dev");
        group2.setMembers(membersGroup2);

        List<AcmUser> expectedUserList = Arrays.asList(user1, user2, user3, user4);

        List<String> rolesForPrivilege = Arrays.asList(role1, role2);
        Map<String, List<String>> rolesToGroups = new HashMap<>();
        rolesToGroups.put(role1, Arrays.asList(group1.getName()));
        rolesToGroups.put(role2, Arrays.asList(group2.getName()));

        expect(mockAuthentication.getName()).andReturn("user");
        expect(mockPluginManager.getRolesForPrivilege(privilege)).andReturn(rolesForPrivilege);

        // this is a unit test, so we are only verifying our class returns the results it got from the
        // functional access control service.  We need unit tests on the functional access control service, to
        // ensure it is working correctly.
        expect(mockFunctionalAccessService.getApplicationRolesToGroups()).andReturn(rolesToGroups);
        expect(mockFunctionalAccessService.getUsersByRolesAndGroups(rolesForPrivilege, rolesToGroups, null, null)).andReturn(group2.getMembers());

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/latest/service/functionalaccess/users/{privilege}", privilege)
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockAuthentication))
                .andReturn();

        verifyAll();

        LOG.info("Results: " + result.getResponse().getContentAsString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<AcmUser> resultUserList = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ArrayList<AcmUser>>()
        {
        });

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(group2.getMembers().size(), resultUserList.size());

    }

    @Test
    public void usersByPrivilegeAndGroupTest() throws Exception
    {

        String privilege = "acm-privilege";
        String role1 = "ROLE_ADMINISTRATOR";
        String role2 = "ROLE_INVESTIGATOR_SUPERVISOR";


        // Group 1
        Set<AcmUser> membersGroup1 = new HashSet<>();

        AcmUser user1 = new AcmUser();
        user1.setUserId("user1");

        AcmUser user2 = new AcmUser();
        user2.setUserId("user2");

        membersGroup1.addAll(Arrays.asList(user1, user2));

        AcmGroup group1 = new AcmGroup();
        group1.setName("acm_administrator_dev");
        group1.setMembers(membersGroup1);


        // Group 2
        Set<AcmUser> membersGroup2 = new HashSet<>();

        AcmUser user3 = new AcmUser();
        user3.setUserId("user3");

        AcmUser user4 = new AcmUser();
        user4.setUserId("user4");

        membersGroup2.addAll(Arrays.asList(user3, user4));

        AcmGroup group2 = new AcmGroup();
        group2.setName("acm_supervisor_dev");
        group2.setMembers(membersGroup2);


        List<AcmUser> expectedUserList = Arrays.asList(user1, user2);


        List<String> rolesForPrivilege = Arrays.asList(role1, role2);
        Map<String, List<String>> rolesToGroups = new HashMap<>();
        rolesToGroups.put(role1, Arrays.asList(group1.getName()));
        rolesToGroups.put(role2, Arrays.asList(group2.getName()));


        expect(mockAuthentication.getName()).andReturn("user");
        expect(mockPluginManager.getRolesForPrivilege(privilege)).andReturn(rolesForPrivilege);

        expect(mockFunctionalAccessService.getApplicationRolesToGroups()).andReturn(rolesToGroups);
        expect(mockFunctionalAccessService.getUsersByRolesAndGroups(rolesForPrivilege, rolesToGroups, group1.getName(), null)).andReturn(group1.getMembers());

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/latest/service/functionalaccess/users/{privilege}/{group}", privilege, group1.getName())
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockAuthentication))
                .andReturn();

        verifyAll();

        LOG.info("Results: " + result.getResponse().getContentAsString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<AcmUser> resultUserList = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ArrayList<AcmUser>>()
        {
        });

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(expectedUserList.size(), resultUserList.size());
        for (AcmUser expected : expectedUserList)
        {
            assertTrue(resultUserList.contains(expected));
        }

    }

}
