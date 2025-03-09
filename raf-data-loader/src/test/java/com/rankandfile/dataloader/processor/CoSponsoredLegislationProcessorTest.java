package com.rankandfile.dataloader.processor;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.entity.SponsoredLegislation;
import com.rankandfile.dataloader.repository.PersonRepository;
import com.rankandfile.dataloader.repository.SponsoredLegislationRepository;
import com.rankandfile.dataloader.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoSponsoredLegislationProcessorTest {

    @Mock
    private SponsoredLegislationRepository sponsoredLegislationRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private CoSponsoredLegislationProcessor coSponsoredLegislationProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessWithValidCosponsors() {
        Bill bill = new Bill();
        bill.setBillId("BILL-XYZ");
        bill.setCongress("117");
        bill.setBillNo("1234");
        bill.setBillType("H.R.");

        String json = "{\n" +
                "  \"cosponsors\": [\n" +
                "    { \"bioguideId\": \"P1234\" },\n" +
                "    { \"bioguideId\": \"P5678\" }\n" +
                "  ]\n" +
                "}";

        // Existing relationships in DB (empty for this test, so all are new)
        when(sponsoredLegislationRepository.findByBillBillIdAndSponsorType("BILL-XYZ", "Co-Sponsor"))
                .thenReturn(Collections.emptyList());

        // Mock persons found in DB
        Person person1 = new Person();
        person1.setPersonId("P1234");

        Person person2 = new Person();
        person2.setPersonId("P5678");

        when(personRepository.findPersonByPersonId("P1234")).thenReturn(person1);
        when(personRepository.findPersonByPersonId("P5678")).thenReturn(person2);

        // Mock ID generator
        when(idGenerator.generateSponsLegId())
                .thenReturn("SponsLeg-1", "SponsLeg-2");

        List<SponsoredLegislation> result = coSponsoredLegislationProcessor.process(json, bill);

        verify(sponsoredLegislationRepository)
                .findByBillBillIdAndSponsorType("BILL-XYZ", "Co-Sponsor");

        // We expect two new relationships because none existed before
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify that each new SponsoredLegislation has the correct data
        SponsoredLegislation sponsored1 = result.get(0);
        assertEquals("SponsLeg-1", sponsored1.getSponLegId());
        assertEquals(person1, sponsored1.getPerson());
        assertEquals(bill, sponsored1.getBill());
        assertEquals("Co-Sponsor", sponsored1.getSponsorType());

        SponsoredLegislation sponsored2 = result.get(1);
        assertEquals("SponsLeg-2", sponsored2.getSponLegId());
        assertEquals(person2, sponsored2.getPerson());
        assertEquals(bill, sponsored2.getBill());
        assertEquals("Co-Sponsor", sponsored2.getSponsorType());
    }

    @Test
    void testProcessWithNoCosponsorsField() {
        Bill bill = new Bill();
        bill.setBillId("BILL-XYZ");
        String json = "{ }"; // no "cosponsors" field

        List<SponsoredLegislation> result = coSponsoredLegislationProcessor.process(json, bill);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        // No repository interactions expected
        verifyNoInteractions(sponsoredLegislationRepository, personRepository);
    }

    @Test
    void testProcessWithEmptyCosponsorsArray() {
        Bill bill = new Bill();
        bill.setBillId("BILL-XYZ");
        String json = "{ \"cosponsors\": [] }";

        List<SponsoredLegislation> result = coSponsoredLegislationProcessor.process(json, bill);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoMoreInteractions(sponsoredLegislationRepository, personRepository);
    }

    @Test
    void testProcessWithInvalidJson() {
        Bill bill = new Bill();
        bill.setBillId("BILL-XYZ");
        String invalidJson = "{ \"cosponsors\": [ ";

        List<SponsoredLegislation> result = coSponsoredLegislationProcessor.process(invalidJson, bill);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(sponsoredLegislationRepository, personRepository);
    }

    @Test
    void testProcessWithExistingRelationship() {
        Bill bill = new Bill();
        bill.setBillId("BILL-XYZ");
        bill.setCongress("117");
        bill.setBillNo("1234");
        bill.setBillType("H.R.");

        String json = "{\n" +
                "  \"cosponsors\": [\n" +
                "    { \"bioguideId\": \"P1234\" }\n" +
                "  ]\n" +
                "}";

        // Existing relationship in DB
        Person existingPerson = new Person();
        existingPerson.setPersonId("P1234");

        SponsoredLegislation existingSl = new SponsoredLegislation();
        existingSl.setSponLegId("SponsLeg-Existing");
        existingSl.setPerson(existingPerson);
        existingSl.setBill(bill);
        existingSl.setSponsorType("Co-Sponsor");

        when(sponsoredLegislationRepository.findByBillBillIdAndSponsorType("BILL-XYZ", "Co-Sponsor"))
                .thenReturn(Collections.singletonList(existingSl));

        when(personRepository.findPersonByPersonId("P1234")).thenReturn(existingPerson);

        List<SponsoredLegislation> result = coSponsoredLegislationProcessor.process(json, bill);

        // Relationship already exists, so no new entity should be created
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SponsLeg-Existing", result.get(0).getSponLegId());

        verify(sponsoredLegislationRepository).findByBillBillIdAndSponsorType("BILL-XYZ", "Co-Sponsor");
        verify(personRepository).findPersonByPersonId("P1234");
        verifyNoMoreInteractions(sponsoredLegislationRepository);
    }

    @Test
    void testProcessWithUnknownCosponsor() {
        Bill bill = new Bill();
        bill.setBillId("BILL-XYZ");
        bill.setCongress("117");
        bill.setBillNo("1234");
        bill.setBillType("H.R.");

        String json = "{\n" +
                "  \"cosponsors\": [\n" +
                "    { \"bioguideId\": \"UNKNOWN\" }\n" +
                "  ]\n" +
                "}";

        // No existing relationships
        when(sponsoredLegislationRepository.findByBillBillIdAndSponsorType("BILL-XYZ", "Co-Sponsor"))
                .thenReturn(Collections.emptyList());

        // Unknown cosponsor in PersonRepository
        when(personRepository.findPersonByPersonId("UNKNOWN")).thenReturn(null);

        List<SponsoredLegislation> result = coSponsoredLegislationProcessor.process(json, bill);

        assertNotNull(result);
        // Should skip creation because Person is not found
        assertTrue(result.isEmpty());

        verify(sponsoredLegislationRepository)
                .findByBillBillIdAndSponsorType("BILL-XYZ", "Co-Sponsor");
        verify(personRepository).findPersonByPersonId("UNKNOWN");
        verifyNoMoreInteractions(sponsoredLegislationRepository, personRepository);
    }
}
