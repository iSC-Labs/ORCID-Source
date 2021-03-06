package org.orcid.core.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ehcache.Cache;
import org.orcid.core.manager.ActivityManager;
import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.core.manager.ProfileEntityManager;
import org.orcid.jaxb.model.common_v2.Contributor;
import org.orcid.jaxb.model.common_v2.CreditName;
import org.orcid.jaxb.model.record.bulk.BulkElement;
import org.orcid.jaxb.model.record_v2.Funding;
import org.orcid.jaxb.model.record_v2.FundingContributor;
import org.orcid.jaxb.model.record_v2.Work;
import org.orcid.jaxb.model.record_v2.WorkBulk;
import org.orcid.persistence.aop.ProfileLastModifiedAspect;
import org.orcid.persistence.dao.RecordNameDao;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.persistence.jpa.entities.RecordNameEntity;
import org.orcid.pojo.ajaxForm.PojoUtil;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Iterables;

public class ContributorUtils {
    
    private final Integer BATCH_SIZE;           

    private ProfileEntityCacheManager profileEntityCacheManager;

    private ActivityManager cacheManager;

    private ProfileEntityManager profileEntityManager;

    private RecordNameDao recordNameDao; 
    
    private Cache<String, String> contributorsNameCache;
    
    protected ProfileLastModifiedAspect profileLastModifiedAspect;
    
    public ContributorUtils(@Value("${org.orcid.contributor.names.batch_size:2500}") Integer batchSize) {
        if(batchSize == null) {
            BATCH_SIZE = 2500;
        } else {
            BATCH_SIZE = batchSize;
        }
    }
    
    public void filterContributorPrivateData(Funding funding) {
        if (funding.getContributors() != null && funding.getContributors().getContributor() != null) {
            for (FundingContributor contributor : funding.getContributors().getContributor()) {
                contributor.setContributorEmail(null);
                if (!PojoUtil.isEmpty(contributor.getContributorOrcid())) {
                    String contributorOrcid = contributor.getContributorOrcid().getPath();
                    if (profileEntityManager.orcidExists(contributorOrcid)) {
                        // contributor is an ORCID user - visibility of user's
                        // name in record must be taken into account
                        ProfileEntity profileEntity = profileEntityCacheManager.retrieve(contributorOrcid);
                        String publicContributorCreditName = cacheManager.getPublicCreditName(profileEntity);
                        CreditName creditName = new CreditName(publicContributorCreditName != null ? publicContributorCreditName : "");
                        contributor.setCreditName(creditName);
                    }
                }
            }
        }
    }

    public void filterContributorPrivateData(Work work) {
        if (work.getWorkContributors() != null && work.getWorkContributors().getContributor() != null) {
            List<Contributor> contributorList = work.getWorkContributors().getContributor();
            List<Contributor> contributorsToPopulateName = new ArrayList<Contributor>();
            Set<String> idsToPopulateName = new HashSet<String>();
            // Populate the credit name of cached contributors and populate the list of names to retrive from the DB
            for (Contributor contributor : contributorList) {
                contributor.setContributorEmail(null);
                if (!PojoUtil.isEmpty(contributor.getContributorOrcid())) {
                    String orcid = contributor.getContributorOrcid().getPath();
                    String cachedName = getCachedContributorName(orcid);
                    if(cachedName == null) {
                        idsToPopulateName.add(orcid);
                        contributorsToPopulateName.add(contributor);
                    } else {
                        CreditName creditName = new CreditName(cachedName);
                        contributor.setCreditName(creditName);
                    }                    
                }
            }
            
            // Fetch the contributor names
            Map<String, String> contributorNames = getContributorNamesFromDB(idsToPopulateName);
            
            // Populate missing names
            for(Contributor contributor : contributorsToPopulateName) {
                String orcid = contributor.getContributorOrcid().getPath();
                // If the key doesn't exists in the name, it means the name is private or the orcid id doesn't exists
                if(contributorNames.containsKey(orcid)) {
                    String name = contributorNames.get(orcid);                    
                    CreditName creditName = new CreditName(name);
                    contributor.setCreditName(creditName);                    
                } 
            }
        }
    }
    
    private String getCachedContributorName(String orcid) {
        String cacheKey = getCacheKey(orcid);
        if(contributorsNameCache.containsKey(cacheKey)){
            return contributorsNameCache.get(cacheKey);
        }        
        
        return null;
    }
    
    private Map<String, String> getContributorNamesFromDB(Set<String> ids) {
        Iterable<List<String>> it = Iterables.partition(ids, BATCH_SIZE);
        Map<String, String> contributorNames = new HashMap<String, String>();
        for(List<String> idsList : it) {
            List<RecordNameEntity> entities = recordNameDao.getRecordNames(idsList);
            if(entities != null) {
                for(RecordNameEntity entity : entities) {
                    String orcid = entity.getProfile().getId();
                    String publicCreditName = cacheManager.getPublicCreditName(entity);
                    publicCreditName = (publicCreditName == null ? "" : publicCreditName);
                    contributorNames.put(orcid, publicCreditName);
                    // Store in the cache
                    contributorsNameCache.put(getCacheKey(orcid), publicCreditName);
                }
            }
        }
        return contributorNames;
    }

    public void filterContributorPrivateData(WorkBulk works) {
        if(works != null) {
            for(BulkElement element : works.getBulk()) {
                if(Work.class.isAssignableFrom(element.getClass())) {
                    filterContributorPrivateData((Work) element);
                }
            }
        }
    }
    
    private String getCacheKey(String orcid) {
        Date lastModified = profileLastModifiedAspect.retrieveLastModifiedDate(orcid);
        return orcid + "_" + (lastModified == null ? 0 : lastModified.getTime());
    }
    
    public void setProfileEntityCacheManager(ProfileEntityCacheManager profileEntityCacheManager) {
        this.profileEntityCacheManager = profileEntityCacheManager;
    }

    public void setCacheManager(ActivityManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setProfileEntityManager(ProfileEntityManager profileEntityManager) {
        this.profileEntityManager = profileEntityManager;
    }

    public void setRecordNameDao(RecordNameDao recordNameDao) {
        this.recordNameDao = recordNameDao;
    }

    public void setContributorsNameCache(Cache<String, String> contributorsNameCache) {
        this.contributorsNameCache = contributorsNameCache;
    }

    public void setProfileLastModifiedAspect(ProfileLastModifiedAspect profileLastModifiedAspect) {
        this.profileLastModifiedAspect = profileLastModifiedAspect;
    }        
}
