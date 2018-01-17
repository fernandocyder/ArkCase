package com.armedia.acm.plugins.objectassociation.dao;

import com.armedia.acm.data.AcmAbstractDao;
import com.armedia.acm.plugins.objectassociation.model.ObjectAssociation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

import java.util.List;

/**
 * Created by armdev on 11/6/14.
 */
public class ObjectAssociationDao extends AcmAbstractDao<ObjectAssociation>
{
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Override
    protected Class<ObjectAssociation> getPersistenceClass()
    {
        return ObjectAssociation.class;
    }

    @Transactional
    public List<ObjectAssociation> findByParentTypeAndId(String parentType, Long parentId)
    {
        TypedQuery<ObjectAssociation> findByParentTypeAndId = getEm().createQuery(
                "SELECT e " +
                        "FROM ObjectAssociation e " +
                        "WHERE e.parentType = :parentType " +
                        "AND e.parentId = :parentId " +
                        "ORDER BY e.targetName",
                ObjectAssociation.class);

        findByParentTypeAndId.setParameter("parentId", parentId);
        findByParentTypeAndId.setParameter("parentType", parentType);

        List<ObjectAssociation> retval = findByParentTypeAndId.getResultList();

        return retval;

    }

    public ObjectAssociation findChildOfType(String parentType, Long parentId, String targetSubtype)
    {
        TypedQuery<ObjectAssociation> selectQuery = getEm().createQuery(
                "SELECT e " +
                        "FROM ObjectAssociation e " +
                        "WHERE e.parentType = :parentType " +
                        "AND e.parentId = :parentId " +
                        "AND e.targetType = :targetType " +
                        "AND e.category = :targetCategory " +
                        "AND e.targetSubtype = :targetSubtype " +
                        "ORDER BY e.targetName",
                ObjectAssociation.class);

        selectQuery.setParameter("parentId", parentId);
        selectQuery.setParameter("parentType", parentType);
        selectQuery.setParameter("targetType", "FILE");
        selectQuery.setParameter("targetCategory", "DOCUMENT");
        selectQuery.setParameter("targetSubtype", targetSubtype);

        ObjectAssociation retval = null;

        try
        {
            retval = selectQuery.getSingleResult();
        }
        catch (Exception e)
        {
            LOG.error("Cannot find Object Association for parentId=[{}], parentType=[{}] and targetSubtype=[{}].", parentId, parentType,
                    targetSubtype, e);
        }

        return retval;

    }

    @Transactional
    public void delete(Long id)
    {
        ObjectAssociation objectAssociation = getEm().find(getPersistenceClass(), id);
        getEm().remove(objectAssociation);
    }
}
