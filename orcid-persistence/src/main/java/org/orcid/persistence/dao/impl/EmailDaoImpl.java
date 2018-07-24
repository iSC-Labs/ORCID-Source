package org.orcid.persistence.dao.impl;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.orcid.persistence.dao.EmailDao;
import org.orcid.persistence.jpa.entities.EmailEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 
 * @author Will Simpson
 * 
 */
public class EmailDaoImpl extends GenericDaoImpl<EmailEntity, String> implements EmailDao {
    
    private static final String PUBLIC_VISIBILITY = "PUBLIC";

    private static final String PRIVATE_VISIBILITY = "PRIVATE";
    
    public EmailDaoImpl() {
        super(EmailEntity.class);
    }

    @Override
    public boolean emailExists(String emailHash) {
        Assert.hasText(emailHash, "Cannot check for an empty email hash");
        TypedQuery<Long> query = entityManager.createQuery("select count(email) from EmailEntity where id = :emailHash", Long.class);
        query.setParameter("emailHash", emailHash);
        Long result = query.getSingleResult();
        return (result != null && result > 0);
    }

    @Override
    public EmailEntity findCaseInsensitive(String email) {
        Assert.hasText(email, "Cannot find using an empty email address");
        TypedQuery<EmailEntity> query = entityManager.createQuery("from EmailEntity where trim(lower(email)) = trim(lower(:email))", EmailEntity.class);
        query.setParameter("email", email);
        List<EmailEntity> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public String findOrcidIdByCaseInsenitiveEmail(String email) {
        TypedQuery<String> query = entityManager.createQuery("select profile.id from EmailEntity where trim(lower(email)) = trim(lower(:email))", String.class);
        query.setParameter("email", email);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void updateEmail(String orcid, String email, boolean isCurrent, String visibility) {
        Query query = entityManager.createQuery("update EmailEntity set current = :current, visibility = :visibility where orcid = :orcid and email = :email");
        query.setParameter("orcid", orcid);
        query.setParameter("email", email);
        query.setParameter("current", isCurrent);
        query.setParameter("visibility", visibility);
        query.executeUpdate();
    }

    @Override
    @Transactional
    public void updatePrimary(String orcid, String primaryEmail) {
        Query query = entityManager.createNativeQuery("UPDATE email SET is_primary= CASE email WHEN :primaryEmail THEN true ELSE false END WHERE orcid = :orcid");
        query.setParameter("orcid", orcid);        
        query.setParameter("primaryEmail", primaryEmail);
        query.executeUpdate();        
    }

    @Override
    @Transactional
    public void addEmail(String orcid, String email, String emailHash, String visibility, String sourceId, String clientSourceId) {
        try {
            Query query = entityManager
                    .createNativeQuery("INSERT INTO email (date_created, last_modified, orcid, email, email_hash, is_primary, is_verified, is_current, visibility, source_id, client_source_id) VALUES (now(), now(), :orcid, :email, :hash, false, :isVerified, :isCurrent, :visibility, :sourceId, :clientSourceId)");
            query.setParameter("orcid", orcid);
            query.setParameter("email", email);
            query.setParameter("hash", emailHash);
            query.setParameter("visibility", visibility);
            query.setParameter("sourceId", sourceId);
            query.setParameter("clientSourceId", clientSourceId);
            query.setParameter("isVerified", false);
            query.setParameter("isCurrent", true);
            query.executeUpdate();
        } catch(Exception psqle) {
            throw psqle;
        }
    }

    @Override
    @Transactional
    public void removeEmail(String orcid, String email) {
        removeEmail(orcid, email, false);
    }

    @Override
    @Transactional
    public void removeEmail(String orcid, String email, boolean removeIfPrimary) {
        String deleteEmailEvent  = null;
        String deleteEmail = null;
        if (removeIfPrimary) {
            deleteEmailEvent = "delete from email_event where trim(lower(email)) = trim(lower(:email))";
            deleteEmail = "delete from email where orcid = :orcid and trim(lower(email)) = trim(lower(:email))";
        } else {
            //Check if the email is primary before removing the email events and the email itself 
            deleteEmailEvent = "delete from email_event where trim(lower(email)) = trim(lower(:email)) and not(select is_primary from email where trim(lower(email)) = trim(lower(:email)))";
            deleteEmail = "delete from email where orcid = :orcid and trim(lower(email)) = trim(lower(:email)) and not(is_primary);";
        }
        
        Query query = entityManager.createNativeQuery(deleteEmailEvent);        
        query.setParameter("email", email);                
        query.executeUpdate();
        
        query = entityManager.createNativeQuery(deleteEmail);
        query.setParameter("email", email);
        query.setParameter("orcid", orcid);
        query.executeUpdate();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List findIdByCaseInsensitiveEmail(List<String> emails) {
        for (int i=0; i < emails.size(); i++) {
            if (emails.get(i) != emails.get(i).toLowerCase().trim()) emails.set(i, emails.get(i).toLowerCase().trim());
        }
        Query query = entityManager.createNativeQuery("select orcid, email from email where trim(lower(email)) in :emails");        
        query.setParameter("emails", emails);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public void addSourceToEmail(String sourceId, String email) {
        Query query = entityManager.createNativeQuery("update email set source_id = :sourceId where email=:email");
        query.setParameter("sourceId", sourceId);
        query.setParameter("email", email);
        query.executeUpdate();        
    }
    
    @Override
    @Transactional
    public boolean verifyEmail(String email) {
        Query query = entityManager.createNativeQuery("update email set is_verified = true, is_current=true, last_modified=now() where trim(lower(email)) = trim(lower(:email))");
        query.setParameter("email", email);
        return query.executeUpdate() > 0;
    }
    
    @Override
    public boolean isPrimaryEmailVerified(String orcid) {
        Query query = entityManager.createNativeQuery("select is_verified from email where orcid=:orcid and is_primary=true");
        query.setParameter("orcid", orcid);
        return (Boolean)query.getSingleResult();
    }
    
    @Override
    @Transactional
    public boolean verifyPrimaryEmail(String orcid) {
        Query query = entityManager.createNativeQuery("update email set is_verified=true where orcid=:orcid and is_primary=true");
        query.setParameter("orcid", orcid);
        return query.executeUpdate() > 0;
    }
    
    @Override
    @Transactional
    public boolean moveEmailToOtherAccountAsNonPrimary(String email, String origin, String destination) {
        Query query = entityManager.createNativeQuery("update email set orcid=:destination, is_primary=false, last_modified=now() where orcid=:origin and email=:email");
        query.setParameter("destination", destination);
        query.setParameter("origin", origin);
        query.setParameter("email", email);
        return query.executeUpdate() > 0;
    }
    
    @Override
    @Cacheable(value = "emails", key = "#orcid.concat('-').concat(#lastModified)")
    public List<EmailEntity> findByOrcid(String orcid, long lastModified) {
        TypedQuery<EmailEntity> query = entityManager.createQuery("from EmailEntity where orcid = :orcid", EmailEntity.class);
        query.setParameter("orcid", orcid);
        List<EmailEntity> results = query.getResultList();
        return results.isEmpty() ? null : results;
    }
    
    @Override
    @Cacheable(value = "public-emails", key = "#orcid.concat('-').concat(#lastModified)")
    public List<EmailEntity> findPublicEmails(String orcid, long lastModified) {
        return findByOrcid(orcid, PUBLIC_VISIBILITY);
    }
    
    @Override
    public List<EmailEntity> findByOrcid(String orcid, String visibility) {
        TypedQuery<EmailEntity> query = entityManager.createQuery("from EmailEntity where orcid = :orcid and visibility = :visibility", EmailEntity.class);
        query.setParameter("orcid", orcid);
        query.setParameter("visibility", visibility);
        List<EmailEntity> results = query.getResultList();
        return results.isEmpty() ? null : results;
    }

    @Override
    @Transactional
    public boolean updateVerifySetCurrentAndPrimary(String orcid, String email) {
        Query query = entityManager.createQuery("update EmailEntity set current = true, primary = true, verified = true where orcid = :orcid and email = :email");
        query.setParameter("orcid", orcid);
        query.setParameter("email", email);
        return query.executeUpdate() > 0;
    }

    /***
     * Indicates if the given email address could be auto deprecated given the
     * ORCID rules. See
     * https://trello.com/c/ouHyr0mp/3144-implement-new-auto-deprecate-workflow-
     * for-members-unclaimed-ids
     * 
     * @param email
     *            Email address
     * @return true if the email exists, the owner is not claimed and the
     *         client source of the record allows auto deprecating records
     */
    @Override
    public boolean isAutoDeprecateEnableForEmailUsingHash(String emailHash) {
        Query query = entityManager.createNativeQuery("SELECT allow_auto_deprecate FROM client_details WHERE client_details_id=(SELECT client_source_id FROM profile WHERE orcid=(SELECT orcid FROM email WHERE email_hash = :emailHash) AND claimed = false)");
        query.setParameter("emailHash", emailHash);
        try {
            Boolean result = (Boolean) query.getSingleResult();
            return result == null ? false : result;
        } catch(Exception e) {
            //TODO log exception
        }
        return false;
    }
    
    @Override
    public boolean isPrimaryEmail(String email) {
        Query query = entityManager.createNativeQuery("select is_primary from email where email=:email");
        query.setParameter("email", email);
        try {
            Boolean result = (Boolean) query.getSingleResult();
            return result == null ? false : result;
        } catch (Exception e) {

        }
        return false;
    }
    
    @Override
    public boolean isPrimaryEmail(String orcid, String email) {
        Query query = entityManager.createNativeQuery("select is_primary from email where orcid=:orcid and email=:email");
        query.setParameter("orcid", orcid);
        query.setParameter("email", email);
        try {
            Boolean result = (Boolean)query.getSingleResult();
            return result == null ? false : result;
        } catch(Exception e) {
            
        }
        return false;
    }

    @Override
    public EmailEntity findPrimaryEmail(String orcid) {
        TypedQuery<EmailEntity> query = entityManager.createQuery("from EmailEntity where orcid = :orcid and primary = true", EmailEntity.class);
        query.setParameter("orcid", orcid);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public boolean hideAllEmails(String orcid) {
        Query query = entityManager.createQuery("update EmailEntity set visibility = :visibility, lastModified=now() where orcid = :orcid");
        query.setParameter("orcid", orcid);
        query.setParameter("visibility", PRIVATE_VISIBILITY);
        return query.executeUpdate() > 0;
    }

    @Override
    @Transactional
    public boolean updateVisibility(String orcid, String email, String visibility) {
        Query query = entityManager.createQuery("update EmailEntity set visibility = :visibility, lastModified=now() where email = :email and orcid = :orcid");
        query.setParameter("orcid", orcid);
        query.setParameter("email", email);
        query.setParameter("visibility", visibility);
        return query.executeUpdate() > 0;
    }

    @Override
    public List<String> getEmailsToHash(Integer batchSize) {
        Query query = entityManager.createNativeQuery("select id from email where email_hash is null", String.class);
        query.setMaxResults(batchSize);
        return query.getResultList();
    }
    
    @Override    
    public boolean populateEmailHash(String email, String emailHash) {
        Query query = entityManager.createQuery("update EmailEntity set id=:hash where email = :email");        
        query.setParameter("email", email);
        query.setParameter("hash", emailHash);
        return query.executeUpdate() > 0;
    }

    @Override
    public EmailEntity findByEmail(String email) {
        TypedQuery<EmailEntity> query = entityManager.createQuery("from EmailEntity where email = :email", EmailEntity.class);
        query.setParameter("email", email);
        List<EmailEntity> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }       
}
