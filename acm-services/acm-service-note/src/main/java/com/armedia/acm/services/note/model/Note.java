package com.armedia.acm.services.note.model;

/*-
 * #%L
 * ACM Service: Note
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

import com.armedia.acm.core.AcmObject;
import com.armedia.acm.core.AcmParentObjectInfo;
import com.armedia.acm.data.AcmEntity;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "acm_note")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "className", defaultImpl = Note.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "cm_class_name", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("com.armedia.acm.services.note.model.Note")
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Note implements Serializable, AcmObject, AcmEntity, AcmParentObjectInfo
{
    private static final long serialVersionUID = -1154137631399833851L;

    @Id
    @TableGenerator(name = "acm_note_gen", table = "acm_note_id", pkColumnName = "cm_seq_name", valueColumnName = "cm_seq_num", pkColumnValue = "acm_note", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "acm_note_gen")
    @Column(name = "cm_note_id")
    private Long id;

    @Lob
    @Column(name = "cm_note_text")
    private String note;

    @Column(name = "cm_note_type")
    private String type;

    @Column(name = "cm_note_creator", nullable = false, insertable = true, updatable = false)
    private String creator;

    @Column(name = "cm_note_created", nullable = false, insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "cm_parent_object_id")
    private Long parentId;

    @Column(name = "cm_parent_object_type")
    private String parentType;

    @Column(name = "cm_parent_object_title")
    private String parentTitle;

    @Column(name = "cm_note_tag")
    private String tag;

    @Column(name = "cm_note_modifier", nullable = false)
    private String modifier;

    @Column(name = "cm_note_modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "cm_note_author")
    private String author;

    @Column(name = "cm_class_name")
    private String className = this.getClass().getName();

    @PrePersist
    public void beforeInsert()
    {
        Date today = new Date();
        setCreated(today);

        if (null == getType())
        {
            setType(NoteConstants.NOTE_GENERAL);
        }
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String getCreator()
    {
        return creator;
    }

    @Override
    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    @Override
    public Date getCreated()
    {
        return created;
    }

    @Override
    public void setCreated(Date created)
    {
        this.created = created;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getParentType()
    {
        return parentType;
    }

    public void setParentType(String parentType)
    {
        this.parentType = parentType;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    @Override
    public String getModifier()
    {
        return modifier;
    }

    @Override
    public void setModifier(String modifier)
    {
        this.modifier = modifier;
    }

    @Override
    public Date getModified()
    {
        return modified;
    }

    @Override
    public void setModified(Date modified)
    {
        this.modified = modified;
    }

    @Override
    @JsonIgnore
    public String getObjectType()
    {
        return NoteConstants.OBJECT_TYPE;
    }

    @Override
    @JsonIgnore
    public Long getParentObjectId()
    {
        return parentId;
    }

    @Override
    @JsonIgnore
    public String getParentObjectType()
    {
        return parentType;
    }

    public String getParentTitle()
    {
        return parentTitle;
    }

    public void setParentTitle(String parentTitle)
    {
        this.parentTitle = parentTitle;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }
}
