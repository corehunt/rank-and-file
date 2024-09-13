package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.repository.SponsoredLegislationRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonSponsoredLegislationProcessorTest {

    @Mock
    private SponsoredLegislationRepository sponsoredLegislationRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private SponsoredLegislationProcessor sponsoredLegislationProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPersonSponsoredLegislationProcessorFullJson() {
        String json = "{"
                + "\"sponsoredLegislation\": ["
                + "{"
                + "\"congress\": 117,"
                + "\"introducedDate\": \"2022-06-16\","
                + "\"latestAction\": {"
                + "\"actionDate\": \"2022-06-16\","
                + "\"text\": \"Read twice and referred to the Committee on the Judiciary.\""
                + "},"
                + "\"number\": \"4417\","
                + "\"policyArea\": { \"name\": \"Commerce\" },"
                + "\"title\": \"Patent Trial and Appeal Board Reform Act of 2022\","
                + "\"type\": \"S\","
                + "\"url\": \"https://api.congress.gov/v3/bill/117/s/4417?format=json\""
                + "}"
                + "]"
                + "}";

        // Mock the Person object
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(idGenerator.generateSponsLegId()).thenReturn("SL657958");

        // Mock sponsoredLegislationRepository.findByPersonPersonId to return an empty list (no existing legislation)
        when(sponsoredLegislationRepository.findByPersonPersonId("L000174")).thenReturn(Collections.emptyList());

        // Call the process method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Verify the interactions and assert the expected results
        assertEquals(1, result.size());
        SponsoredLegislation legislation = result.get(0);
        assertEquals(117, legislation.getCongress());
        assertEquals(4417, legislation.getBillNo());
        assertEquals("Patent Trial and Appeal Board Reform Act of 2022", legislation.getLegTitle());
        assertEquals("S", legislation.getBillType());
        assertEquals(LocalDate.of(2022, 6, 16), legislation.getIntroDt());
        assertEquals("Commerce", legislation.getPolicyArea());
        assertEquals("SL657958", legislation.getSponLegId());
        assertEquals("https://api.congress.gov/v3/bill/117/s/4417?format=json", legislation.getUrlSrc());

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(idGenerator, times(1)).generateSponsLegId();
        verify(sponsoredLegislationRepository, times(1)).findByPersonPersonId("L000174");
    }

    @Test
    void testShouldHandleEmptySponsoredLegislationArray() {
        // JSON with an empty sponsoredLegislation array
        String json = "{"
                + "\"sponsoredLegislation\": []"
                + "}";

        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(sponsoredLegislationRepository.findByPersonPersonId("L000174")).thenReturn(Collections.emptyList());

        // Call the process method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Assert that the result is an empty list
        assertEquals(0, result.size());

        verify(personRepository, never()).findPersonByPersonId("L000174");
        verify(sponsoredLegislationRepository, never()).findByPersonPersonId("L000174");
        verify(idGenerator, never()).generateSponsLegId();
    }

    @Test
    void testShouldHandleMissingOptionalFields() {
        String json = "{"
                + "\"sponsoredLegislation\": ["
                + "{"
                + "\"congress\": 117,"
                + "\"introducedDate\": \"2022-06-16\","
                + "\"number\": \"4417\","
                + "\"title\": \"Patent Trial and Appeal Board Reform Act of 2022\","
                + "\"type\": \"S\","
                + "\"policyArea\": { \"name\": \"Commerce\" },"
                + "\"url\": \"https://api.congress.gov/v3/bill/117/s/4417?format=json\""
                + "}"
                + "]"
                + "}";

        // Mocks
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(idGenerator.generateSponsLegId()).thenReturn("SLEG12345");
        when(sponsoredLegislationRepository.findByPersonPersonId("L000174")).thenReturn(Collections.emptyList());

        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        assertEquals(1, result.size());
        SponsoredLegislation legislation = result.get(0);
        assertNull(legislation.getLatestActionDt());
        assertNull(legislation.getLatestActionTxt());

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(idGenerator, times(1)).generateSponsLegId();
        verify(sponsoredLegislationRepository, times(1)).findByPersonPersonId("L000174");
    }

    @Test
    void testShouldHandleNullValuesInOptionalFields() {
        String json = "{"
                + "\"sponsoredLegislation\": ["
                + "{"
                + "\"congress\": 117,"
                + "\"introducedDate\": \"2022-06-16\","
                + "\"number\": \"4417\","
                + "\"title\": \"Patent Trial and Appeal Board Reform Act of 2022\","
                + "\"type\": \"S\","
                + "\"latestAction\": null,"
                + "\"policyArea\": null,"
                + "\"url\": \"https://api.congress.gov/v3/bill/117/s/4417?format=json\""
                + "}"
                + "]"
                + "}";

        // Mocks
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(idGenerator.generateSponsLegId()).thenReturn("SLEG12345");
        when(sponsoredLegislationRepository.findByPersonPersonId("L000174")).thenReturn(Collections.emptyList());

        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        assertEquals(1, result.size());
        SponsoredLegislation legislation = result.get(0);
        assertNull(legislation.getLatestActionDt());
        assertNull(legislation.getLatestActionTxt());
        assertNull(legislation.getPolicyArea());

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(idGenerator, times(1)).generateSponsLegId();
        verify(sponsoredLegislationRepository, times(1)).findByPersonPersonId("L000174");
    }

    @Test
    void testShouldHandleInvalidDateFormat() {
        String json = "{"
                + "\"sponsoredLegislation\": ["
                + "{"
                + "\"congress\": 117,"
                + "\"introducedDate\": \"invalid-date\","
                + "\"number\": \"4417\","
                + "\"title\": \"Patent Trial and Appeal Board Reform Act of 2022\","
                + "\"type\": \"S\","
                + "\"policyArea\": { \"name\": \"Commerce\" },"
                + "\"url\": \"https://api.congress.gov/v3/bill/117/s/4417?format=json\""
                + "}"
                + "]"
                + "}";

        // Mocks
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(idGenerator.generateSponsLegId()).thenReturn("SLEG12345");
        when(sponsoredLegislationRepository.findByPersonPersonId("L000174")).thenReturn(Collections.emptyList());

        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Verify that one entry is returned despite the invalid date
        assertEquals(1, result.size());

        // Verify the returned entry
        SponsoredLegislation legislation = result.get(0);
        assertEquals(117, legislation.getCongress());
        assertEquals(4417, legislation.getBillNo());
        assertNull(legislation.getIntroDt());  // The intro date should be null due to invalid format
        assertEquals("Patent Trial and Appeal Board Reform Act of 2022", legislation.getLegTitle());
        assertEquals("S", legislation.getBillType());
        assertEquals("Commerce", legislation.getPolicyArea());
        assertEquals("https://api.congress.gov/v3/bill/117/s/4417?format=json", legislation.getUrlSrc());

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(idGenerator, times(1)).generateSponsLegId();
        verify(sponsoredLegislationRepository, times(1)).findByPersonPersonId("L000174");
    }

    @Test
    void testShouldUpdateExistingLegislation() {
        String json = "{"
                + "\"sponsoredLegislation\": ["
                + "{"
                + "\"congress\": 117,"
                + "\"introducedDate\": \"2022-06-16\","
                + "\"number\": \"4417\","
                + "\"title\": \"Updated Legislation Title\","
                + "\"type\": \"S\","
                + "\"latestAction\": {"
                + "\"actionDate\": \"2022-06-16\","
                + "\"text\": \"Read twice and referred to the Committee on the Judiciary.\""
                + "},"
                + "\"policyArea\": { \"name\": \"Commerce\" },"
                + "\"url\": \"https://api.congress.gov/v3/bill/117/s/4417?format=json\""
                + "}"
                + "]"
                + "}";

        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        // Mock an existing SponsoredLegislation object in the database
        SponsoredLegislation existingLegislation = new SponsoredLegislation();
        existingLegislation.setSponLegId("SLEG12345");
        existingLegislation.setCongress(117);
        existingLegislation.setBillNo(4417);
        existingLegislation.setBillType("S"); // Include billType since it's part of the key
        existingLegislation.setLegTitle("Old Legislation Title");

        // Mock repositories and idGenerator
        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(sponsoredLegislationRepository.findByPersonPersonId("L000174")).thenReturn(Collections.singletonList(existingLegislation));
        // Since the legislation already exists, idGenerator should not be called
        when(idGenerator.generateSponsLegId()).thenReturn("NEW_ID");

        // Call the process method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Verify the interactions and assert the expected results
        assertEquals(1, result.size());
        SponsoredLegislation legislation = result.get(0);
        assertEquals("SLEG12345", legislation.getSponLegId());  // Ensure the ID hasn't changed
        assertEquals("Updated Legislation Title", legislation.getLegTitle()); // Title should be updated

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(sponsoredLegislationRepository, times(1)).findByPersonPersonId("L000174");
        verify(idGenerator, never()).generateSponsLegId();  // ID generator should not be called
    }

    @Test
    void testShouldHandleBatchProcessingWithExistingAndNewLegislation() {
        String json = "{"
                + "\"sponsoredLegislation\": ["
                + "{"
                + "\"congress\": 117,"
                + "\"introducedDate\": \"2022-06-16\","
                + "\"number\": \"4417\","
                + "\"title\": \"Existing Legislation Title\","
                + "\"type\": \"S\","
                + "\"latestAction\": {"
                + "\"actionDate\": \"2022-06-16\","
                + "\"text\": \"Some action.\""
                + "},"
                + "\"policyArea\": { \"name\": \"Commerce\" },"
                + "\"url\": \"https://example.com/legislation1\""
                + "},"
                + "{"
                + "\"congress\": 117,"
                + "\"introducedDate\": \"2022-07-01\","
                + "\"number\": \"1234\","
                + "\"title\": \"New Legislation Title\","
                + "\"type\": \"HR\","
                + "\"latestAction\": {"
                + "\"actionDate\": \"2022-07-02\","
                + "\"text\": \"Another action.\""
                + "},"
                + "\"policyArea\": { \"name\": \"Finance\" },"
                + "\"url\": \"https://example.com/legislation2\""
                + "}"
                + "]"
                + "}";

        // Mocks
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        // Mock existing legislation
        SponsoredLegislation existingLegislation = new SponsoredLegislation();
        existingLegislation.setSponLegId("SLEG_EXISTING");
        existingLegislation.setCongress(117);
        existingLegislation.setBillNo(4417);
        existingLegislation.setBillType("S");
        existingLegislation.setLegTitle("Old Title");

        // Mock repositories and idGenerator
        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(sponsoredLegislationRepository.findByPersonPersonId("L000174")).thenReturn(Collections.singletonList(existingLegislation));
        when(idGenerator.generateSponsLegId()).thenReturn("SLEG_NEW");

        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Verify the interactions and assert the expected results
        assertEquals(2, result.size());

        // Check the existing legislation was updated
        SponsoredLegislation updatedLegislation = result.stream()
                .filter(leg -> leg.getSponLegId().equals("SLEG_EXISTING"))
                .findFirst()
                .orElse(null);
        assertNotNull(updatedLegislation);
        assertEquals("Existing Legislation Title", updatedLegislation.getLegTitle());

        // Check the new legislation was added
        SponsoredLegislation newLegislation = result.stream()
                .filter(leg -> leg.getSponLegId().equals("SLEG_NEW"))
                .findFirst()
                .orElse(null);
        assertNotNull(newLegislation);
        assertEquals(117, newLegislation.getCongress());
        assertEquals(1234, newLegislation.getBillNo());
        assertEquals("New Legislation Title", newLegislation.getLegTitle());
        assertEquals("HR", newLegislation.getBillType());

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(sponsoredLegislationRepository, times(1)).findByPersonPersonId("L000174");
        verify(idGenerator, times(1)).generateSponsLegId();
    }
}
