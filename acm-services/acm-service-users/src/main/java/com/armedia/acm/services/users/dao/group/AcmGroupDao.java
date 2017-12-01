package com.armedia.acm.services.users.dao.group;

import com.armedia.acm.data.AcmAbstractDao;
import com.armedia.acm.services.users.model.AcmUser;
import com.armedia.acm.services.users.model.AcmUserRoleState;
import com.armedia.acm.services.users.model.group.AcmGroup;
import com.armedia.acm.services.users.model.group.AcmGroupStatus;
import com.armedia.acm.services.users.model.group.AcmGroupType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author riste.tutureski
 */
public class AcmGroupDao extends AcmAbstractDao<AcmGroup>
{
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Transactional
    public AcmGroup findByName(String name)
    {
        CriteriaBuilder builder = getEm().getCriteriaBuilder();
        CriteriaQuery<AcmGroup> query = builder.createQuery(AcmGroup.class);
        Root<AcmGroup> group = query.from(AcmGroup.class);

        query.select(group);

        query.where(
                builder.and(
                        builder.equal(group.<String>get("name"), name)
                )
        );

        TypedQuery<AcmGroup> dbQuery = getEm().createQuery(query);

        AcmGroup acmGroup = null;

        try
        {
            acmGroup = dbQuery.getSingleResult();
        }
        catch (NoResultException e)
        {
            LOG.warn("There is no group with name [{}]", name);
        }
        catch (NonUniqueResultException e)
        {
            LOG.warn("There is no unique group found with name [{}]. More than one group has this name", name);
        }
        catch (Exception e)
        {
            LOG.error("Error while retrieving group by group name [{}]", name, e);
        }

        return acmGroup;
    }

    @Transactional
    public boolean deleteAcmGroupByName(String name)
    {
        AcmGroup groupToBeDeleted = findByName(name);
        if (groupToBeDeleted != null)
        {
            getEm().remove(groupToBeDeleted);
            return true;
        } else
        {
            return false;
        }
    }

    @Transactional
    public AcmGroup markGroupDeleted(String groupName)
    {
        AcmGroup acmGroup = findByName(groupName);
        if (acmGroup == null) return null;

        acmGroup.setAscendantsList(null);
        acmGroup.setStatus(AcmGroupStatus.DELETE);

        acmGroup.getUserMembers()
                .forEach(user -> user.getGroups().remove(acmGroup));
        acmGroup.getMemberOfGroups()
                .forEach(it -> it.getMemberGroups().remove(acmGroup));
        acmGroup.getMemberGroups()
                .forEach(it -> acmGroup.getMemberOfGroups().remove(acmGroup));

        acmGroup.setUserMembers(new HashSet<>());
        acmGroup.setMemberOfGroups(new HashSet<>());
        acmGroup.setMemberGroups(new HashSet<>());
        return acmGroup;
    }

    public void markRolesByGroupInvalid(String groupName)
    {
        Query markInvalid = getEm().createQuery("UPDATE AcmUserRole aur set aur.userRoleState = :state "
                + "WHERE aur.roleName = :groupName");
        markInvalid.setParameter("state", AcmUserRoleState.INVALID);
        markInvalid.setParameter("groupName", groupName);
        markInvalid.executeUpdate();
    }

    @Transactional
    public AcmGroup removeMembersFromGroup(String name, Set<AcmUser> membersToRemove)
    {
        AcmGroup group = findByName(name);

        if (group != null)
        {
            membersToRemove.forEach(member -> member.getGroups().remove(group));

            Set<AcmUser> userMembers = group.getUserMembers();
            userMembers.removeAll(membersToRemove);
        }
        return group;
    }

    @Transactional
    public List<AcmGroup> findByUserMember(AcmUser user)
    {
        TypedQuery<AcmGroup> query = getEm().createQuery("SELECT group FROM AcmGroup group WHERE group.userMembers = :user",
                AcmGroup.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    private Set<AcmUser> getClonedMembers(Set<AcmUser> members)
    {
        @SuppressWarnings("unchecked")
        Set<AcmUser> clonedMembers = (HashSet) new HashSet<>(members).clone();

        return clonedMembers;
    }

    public List<AcmGroup> findLdapGroupsWithUsersByDirectory(String directoryName)
    {
        TypedQuery<AcmGroup> allLdapGroupsInDirectory = getEm().
                createQuery("SELECT DISTINCT acmGroup FROM AcmGroup acmGroup LEFT JOIN FETCH acmGroup.userMembers "
                                + "WHERE acmGroup.type = com.armedia.acm.services.users.model.group.AcmGroupType.LDAP_GROUP "
                                + "AND acmGroup.directoryName = :directoryName",
                        AcmGroup.class);
       // allLdapGroupsInDirectory.setParameter("groupType", AcmGroupType.LDAP_GROUP);
        allLdapGroupsInDirectory.setParameter("directoryName", directoryName);
        return allLdapGroupsInDirectory.getResultList();
    }

    public List<AcmGroup> findByTypeWithUsers(AcmGroupType type)
    {
        TypedQuery<AcmGroup> query = getEm().createQuery("SELECT DISTINCT acmGroup "
                + "FROM AcmGroup acmGroup "
                + "LEFT JOIN FETCH acmGroup.userMembers "
                + "WHERE acmGroup.type = :groupType", AcmGroup.class);
        query.setParameter("groupType", type);
        return query.getResultList();
    }

    public List<AcmGroup> findByStatusAndType(AcmGroupStatus status, AcmGroupType type)
    {
        TypedQuery<AcmGroup> query = getEm().createQuery("SELECT acmGroup "
                + "FROM AcmGroup acmGroup "
                + "WHERE acmGroup.status = :status "
                + "AND acmGroup.type = :groupType", AcmGroup.class);
        query.setParameter("status", status);
        query.setParameter("groupType", type);
        return query.getResultList();
    }

    public List<AcmGroup> findByTypeAndStatus(AcmGroupType type, AcmGroupStatus status)
    {
        TypedQuery<AcmGroup> findByTypeAndStatusQuery = getEm().
                createQuery("SELECT acmGroup "
                                + "FROM AcmGroup acmGroup "
                                + "WHERE acmGroup.type = :groupType "
                                + "AND acmGroup.status = :status",
                        AcmGroup.class);
        findByTypeAndStatusQuery.setParameter("groupType", type);
        findByTypeAndStatusQuery.setParameter("status", status);
        return findByTypeAndStatusQuery.getResultList();
    }

    @Override
    protected Class<AcmGroup> getPersistenceClass()
    {
        return AcmGroup.class;
    }

}
