/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.core.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.orcid.jaxb.model.record.Funding;
import org.orcid.jaxb.model.record.FundingType;
import org.orcid.jaxb.model.record.Iso3166Country;
import org.orcid.jaxb.model.record.Visibility;
import org.orcid.persistence.jpa.entities.EndDateEntity;
import org.orcid.persistence.jpa.entities.ProfileFundingEntity;
import org.orcid.persistence.jpa.entities.StartDateEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author Angel Montenegro
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:orcid-core-context.xml" })
public class JpaJaxbFundingAdapterTest {

    @Resource
    private JpaJaxbFundingAdapter jpaJaxbFundingAdapter;

    @Test
    public void toFundingEntityTest() throws JAXBException {
        Funding f = getFunding();
        assertNotNull(f);
        ProfileFundingEntity pfe = jpaJaxbFundingAdapter.toProfileFundingEntity(f);
        assertNotNull(pfe);
        // Enums
        assertEquals(Visibility.PRIVATE.value(), pfe.getVisibility().value());
        assertEquals(FundingType.GRANT.value(), pfe.getType().value());

        // General info
        assertEquals(Long.valueOf(123), pfe.getId());
        assertEquals("funding:title", pfe.getTitle());
        assertEquals("funding:translatedTitle", pfe.getTranslatedTitle());
        assertEquals("en", pfe.getTranslatedTitleLanguageCode());
        assertEquals("funding:organizationDefinedType", pfe.getOrganizationDefinedType());
        assertEquals("funding:shortDescription", pfe.getDescription());
        assertEquals("1234", pfe.getNumericAmount().toString());
        assertEquals("ADP", pfe.getCurrencyCode());
        assertEquals("http://tempuri.org", pfe.getUrl());

        // Dates
        assertEquals(Integer.valueOf(25), pfe.getStartDate().getDay());
        assertEquals(Integer.valueOf(1), pfe.getStartDate().getMonth());
        assertEquals(Integer.valueOf(1920), pfe.getStartDate().getYear());
        assertEquals(Integer.valueOf(31), pfe.getEndDate().getDay());
        assertEquals(Integer.valueOf(12), pfe.getEndDate().getMonth());
        assertEquals(Integer.valueOf(2020), pfe.getEndDate().getYear());

        // Contributors
        assertEquals(
                "{\"contributor\":[{\"contributorOrcid\":{\"value\":null,\"valueAsString\":null,\"uri\":\"http://orcid.org/8888-8888-8888-8880\",\"path\":\"8888-8888-8888-8880\",\"host\":\"orcid.org\"},\"creditName\":{\"content\":\"funding:creditName\",\"visibility\":\"PRIVATE\"},\"contributorEmail\":{\"value\":\"funding@contributorEmail.com\"},\"contributorAttributes\":{\"contributorRole\":\"LEAD\"}}]}",
                pfe.getContributorsJson());

        // External identifiers
        assertEquals(
                "{\"fundingExternalIdentifier\":[{\"type\":\"GRANT_NUMBER\",\"value\":\"12345\",\"url\":{\"value\":\"http://tempuri.org\"}},{\"type\":\"GRANT_NUMBER\",\"value\":\"67890\",\"url\":{\"value\":\"http://tempuri.org/2\"}}]}",
                pfe.getExternalIdentifiersJson());

        // Source
        assertEquals("8888-8888-8888-8880", pfe.getSource().getSourceId());

        // Check org values
        assertEquals("common:name", pfe.getOrg().getName());
        assertEquals("common:city", pfe.getOrg().getCity());
        assertEquals("common:region", pfe.getOrg().getRegion());
        assertEquals(Iso3166Country.AF.value(), pfe.getOrg().getCountry().value());
        assertEquals("common:disambiguatedOrganizationIdentifier", pfe.getOrg().getOrgDisambiguated().getSourceId());
        assertEquals("common:disambiguationSource", pfe.getOrg().getOrgDisambiguated().getSourceType());
    }

    @Test
    public void fromFundingEntityTest() throws JAXBException {
        ProfileFundingEntity entity = getProfileFundingEntity();
        assertNotNull(entity);
        assertEquals("123456", entity.getNumericAmount().toString());

        Funding funding = jpaJaxbFundingAdapter.toFunding(entity);
        assertNotNull(funding);
        assertEquals("12345", funding.getPutCode());
        assertNotNull(funding.getAmount());
        assertEquals("123456", funding.getAmount().getContent());
        assertEquals("CRC", funding.getAmount().getCurrencyCode());
        assertNotNull(funding.getContributors());
        assertNotNull(funding.getContributors().getContributor());
        assertEquals(1, funding.getContributors().getContributor().size());
        assertEquals("8888-8888-8888-8880", funding.getContributors().getContributor().get(0).getContributorOrcid().getPath());
        assertEquals("orcid.org", funding.getContributors().getContributor().get(0).getContributorOrcid().getHost());
        assertEquals("http://orcid.org/8888-8888-8888-8880", funding.getContributors().getContributor().get(0).getContributorOrcid().getUri());
        assertEquals("funding:creditName", funding.getContributors().getContributor().get(0).getCreditName().getContent());
        assertEquals(org.orcid.jaxb.model.record.Visibility.PRIVATE, funding.getContributors().getContributor().get(0).getCreditName().getVisibility());
        assertEquals("funding:description", funding.getDescription());
        assertNotNull(funding.getStartDate());
        assertEquals("1", funding.getStartDate().getDay().getValue());
        assertEquals("1", funding.getStartDate().getMonth().getValue());
        assertEquals("2000", funding.getStartDate().getYear().getValue());
        assertNotNull(funding.getEndDate());
        assertEquals("1", funding.getEndDate().getDay().getValue());
        assertEquals("1", funding.getEndDate().getMonth().getValue());
        assertEquals("2020", funding.getEndDate().getYear().getValue());
        assertEquals("funding:title", funding.getTitle().getTitle().getContent());
        assertEquals("funding:translatedTitle", funding.getTitle().getTranslatedTitle().getContent());
        assertEquals("ES", funding.getTitle().getTranslatedTitle().getLanguageCode());
        assertEquals(FundingType.SALARY_AWARD, funding.getType());
        assertEquals(Visibility.PRIVATE, funding.getVisibility());
    }

    private Funding getFunding() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(new Class[] { Funding.class });
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream inputStream = getClass().getResourceAsStream("/record_2.0_rc1/samples/funding-2.0_rc1.xml");
        return (Funding) unmarshaller.unmarshal(inputStream);
    }

    private ProfileFundingEntity getProfileFundingEntity() {
        ProfileFundingEntity result = new ProfileFundingEntity();
        result.setContributorsJson("{\"contributor\":[{\"contributorOrcid\":{\"value\":null,\"valueAsString\":null,\"uri\":\"http://orcid.org/8888-8888-8888-8880\",\"path\":\"8888-8888-8888-8880\",\"host\":\"orcid.org\"},\"creditName\":{\"content\":\"funding:creditName\",\"visibility\":\"PRIVATE\"},\"contributorEmail\":{\"value\":\"funding@contributorEmail.com\"},\"contributorAttributes\":{\"contributorRole\":\"LEAD\"}}]}");
        result.setDescription("funding:description");
        result.setEndDate(new EndDateEntity(2020, 1, 1));
        result.setStartDate(new StartDateEntity(2000, 1, 1));
        result.setExternalIdentifiersJson("{\"fundingExternalIdentifier\":[{\"type\":\"GRANT_NUMBER\",\"value\":\"12345\",\"url\":{\"value\":\"http://tempuri.org\"}},{\"type\":\"GRANT_NUMBER\",\"value\":\"67890\",\"url\":{\"value\":\"http://tempuri.org/2\"}}]}");
        result.setId(12345L);
        result.setNumericAmount(new BigDecimal(123456));
        result.setCurrencyCode("CRC");
        result.setTitle("funding:title");
        result.setTranslatedTitle("funding:translatedTitle");
        result.setTranslatedTitleLanguageCode("ES");
        result.setType(org.orcid.jaxb.model.message.FundingType.SALARY_AWARD);
        result.setVisibility(org.orcid.jaxb.model.message.Visibility.PRIVATE);
        return result;
    }

}
