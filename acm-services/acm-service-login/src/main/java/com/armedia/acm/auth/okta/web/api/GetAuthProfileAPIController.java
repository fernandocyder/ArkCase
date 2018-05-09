package com.armedia.acm.auth.okta.web.api;

/*-
 * #%L
 * ACM Service: User Login and Authentication
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

import com.armedia.acm.auth.okta.model.AuthProfile;
import com.armedia.acm.auth.okta.model.OktaAPIConstants;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by joseph.mcgrady on 3/6/2018.
 */
@Controller
@RequestMapping({ "/api/v1/plugin/okta/authprofile", "/api/latest/plugin/okta/authprofile" })
public class GetAuthProfileAPIController
{
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AuthProfile retrieveAuthProfile()
    {
        String profileName = System.getProperty(OktaAPIConstants.SPRING_PROFILES_ACTIVE);
        if (StringUtils.isEmpty(profileName))
        {
            profileName = OktaAPIConstants.AUTH_DEFAULT_PROFILE;
        }

        AuthProfile authProfile = new AuthProfile();
        authProfile.setName(profileName);
        return authProfile;
    }
}
