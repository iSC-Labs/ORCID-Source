/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2013 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.13 at 11:03:27 AM BST 
//

package org.orcid.jaxb.model.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}work-external-identifier" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.orcid.org/ns/orcid}scope"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "workExternalIdentifier" })
@XmlRootElement(name = "work-external-identifiers")
public class WorkExternalIdentifiers implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @XmlElement(name = "work-external-identifier")
    protected List<WorkExternalIdentifier> workExternalIdentifier;
    @XmlAttribute
    protected Scope scope;
    
    public WorkExternalIdentifiers() {
        
    }
    
    public WorkExternalIdentifiers(List<WorkExternalIdentifier> workExternalIdentifier) {
        this.workExternalIdentifier = workExternalIdentifier;
    }

    /**
     * Gets the value of the workExternalIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the workExternalIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWorkExternalIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WorkExternalIdentifier }
     * 
     * 
     */
    public List<WorkExternalIdentifier> getWorkExternalIdentifier() {
        if (workExternalIdentifier == null) {
            workExternalIdentifier = new ArrayList<WorkExternalIdentifier>();
        }
        return this.workExternalIdentifier;
    }

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link Scope }
     *     
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link Scope }
     *     
     */
    public void setScope(Scope value) {
        this.scope = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkExternalIdentifiers)) {
            return false;
        }

        WorkExternalIdentifiers that = (WorkExternalIdentifiers) o;

        if (scope != that.scope) {
            return false;
        }
        if (workExternalIdentifier != null ? !workExternalIdentifier.equals(that.workExternalIdentifier) : that.workExternalIdentifier != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = workExternalIdentifier != null ? workExternalIdentifier.hashCode() : 0;
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        return result;
    }
}
