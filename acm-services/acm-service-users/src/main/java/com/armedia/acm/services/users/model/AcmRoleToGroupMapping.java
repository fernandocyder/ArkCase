package com.armedia.acm.services.users.model;

/*-
 * #%L
 * ACM Service: Users
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

import com.armedia.acm.services.users.dao.group.AcmGroupDao;
import com.armedia.acm.services.users.model.group.AcmGroup;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AcmRoleToGroupMapping
{
    public static final String GROUP_NAME_WILD_CARD = "@*";
    private Map<String, String> roleToGroupMap;
    private AcmGroupDao groupDao;

    public static Function<String, Stream<String>> mapGroupsString(Function<String, List<AcmGroup>> findGroup)
    {
        return group -> {
            if (group.endsWith(GROUP_NAME_WILD_CARD))
            {
                String groupName = StringUtils.substringBeforeLast(group, GROUP_NAME_WILD_CARD);
                List<AcmGroup> matched = findGroup.apply(groupName);
                return matched.stream()
                        .map(AcmGroup::getName);
            }
            else
            {
                return Stream.of(group);
            }
        };
    }

    public void reloadRoleToGroupMap(Properties properties)
    {
        roleToGroupMap = properties.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));
    }

    public Map<String, List<String>> getGroupToRolesMap()
    {
        // generate all value-key pairs from the original map and then group the keys by these values
        return getRoleToGroupsMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(it -> new AbstractMap.SimpleEntry<>(it, entry.getKey())))
                .collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey,
                        Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toList())));
    }

    public Map<String, Set<String>> getRoleToGroupsMap()
    {
        return getStringSetMap(true);
    }

    public Map<String, Set<String>> getRoleToGroupsMapIgnoreCaseSensitive()
    {
        return getStringSetMap(false);
    }

    private Map<String, Set<String>> getStringSetMap(boolean isUpperCase)
    {
        Map<String, List<AcmGroup>> groupsCache = new HashMap<>();

        Function<String, Set<String>> groupsStringToSet = s -> {
            String[] groupsPerRole = s.split(",");

            return Arrays.stream(groupsPerRole)
                    .filter(StringUtils::isNotBlank)
                    .flatMap(mapGroupsString(name -> groupsCache.computeIfAbsent(name, it -> groupDao.findByMatchingName(it))))
                    .collect(Collectors.toSet());
        };

        return roleToGroupMap.entrySet()
                .stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .collect(
                        Collectors.toMap(entry -> {
                            String roleName = isUpperCase ? entry.getKey().trim().toUpperCase() : entry.getKey().trim();
                            if (!roleName.startsWith("ROLE_"))
                            {
                                roleName = "ROLE_" + roleName;
                            }
                            return roleName;
                        },
                                entry -> groupsStringToSet
                                        .apply(isUpperCase ? entry.getValue().trim().toUpperCase() : entry.getValue().trim())));
    }

    public void setRoleToGroupMap(Map<String, String> roleToGroupMap)
    {
        this.roleToGroupMap = roleToGroupMap;
    }

    public void setGroupDao(AcmGroupDao groupDao)
    {
        this.groupDao = groupDao;
    }
}
