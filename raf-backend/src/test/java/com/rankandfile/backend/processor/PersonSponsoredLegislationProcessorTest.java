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
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PersonSponsoredLegislationProcessorTest {

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
        // Sample JSON input (as a string for the test)
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

        // Mock the behavior of repositories and idGenerator
        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(idGenerator.generateSponsLegId()).thenReturn("SL657958");

        // Mock sponsoredLegislationRepository findByCongressAndBillNo
        when(sponsoredLegislationRepository.findByCongressAndBillNo(117, 4417)).thenReturn(null);

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
        verify(sponsoredLegislationRepository, times(1)).findByCongressAndBillNo(117, 4417);
    }

    @Test
    void process_ShouldHandleEmptySponsoredLegislationArray() {
        // JSON with an empty sponsoredLegislation array
        String json = "{"
                + "\"sponsoredLegislation\": []"
                + "}";

        // Call the process method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Assert that the result is an empty list
        assertEquals(0, result.size());

        // Verify that no repository methods were called since there's no legislation to process
        verify(personRepository, never()).findPersonByPersonId(anyString());
        verify(sponsoredLegislationRepository, never()).findByCongressAndBillNo(anyInt(), anyInt());
        verify(idGenerator, never()).generateSponsLegId();
    }

    @Test
    void process_ShouldHandleMissingOptionalFields() {
        // JSON without the 'latestAction' field
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

        // Mock the Person object
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        // Mock the behavior of repositories and idGenerator
        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(idGenerator.generateSponsLegId()).thenReturn("SLEG12345");
        when(sponsoredLegislationRepository.findByCongressAndBillNo(117, 4417)).thenReturn(null);

        // Call the process method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Verify the interactions and assert the expected results
        assertEquals(1, result.size());
        SponsoredLegislation legislation = result.get(0);
        assertNull(legislation.getLatestActionDt());
        assertNull(legislation.getLatestActionTxt());

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(idGenerator, times(1)).generateSponsLegId();
        verify(sponsoredLegislationRepository, times(1)).findByCongressAndBillNo(117, 4417);
    }

    @Test
    void process_ShouldHandleNullValuesInOptionalFields() {
        // JSON with null values for 'latestAction' and 'policyArea'
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

        // Mock the Person object
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        // Mock the behavior of repositories and idGenerator
        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(idGenerator.generateSponsLegId()).thenReturn("SLEG12345");
        when(sponsoredLegislationRepository.findByCongressAndBillNo(117, 4417)).thenReturn(null);

        // Call the process method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Verify the interactions and assert the expected results
        assertEquals(1, result.size());
        SponsoredLegislation legislation = result.get(0);
        assertNull(legislation.getLatestActionDt());
        assertNull(legislation.getLatestActionTxt());
        assertNull(legislation.getPolicyArea());

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(idGenerator, times(1)).generateSponsLegId();
        verify(sponsoredLegislationRepository, times(1)).findByCongressAndBillNo(117, 4417);
    }

    @Test
    void process_ShouldHandleInvalidDateFormat() {
        // JSON with an invalid date format for 'introducedDate'
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

        // Mock the Person object
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        // Mock the behavior of repositories and idGenerator
        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(idGenerator.generateSponsLegId()).thenReturn("SLEG12345");

        // Expect an exception to be thrown due to the invalid date format
        assertThrows(DateTimeParseException.class, () -> {
            sponsoredLegislationProcessor.process(json, "L000174");
        });

        // Verify that the mocks were called before the exception was thrown
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(idGenerator, times(1)).generateSponsLegId();
        verify(sponsoredLegislationRepository, times(1)).findByCongressAndBillNo(anyInt(), anyInt());
    }

    @Test
    void process_ShouldUpdateExistingLegislation() {
        // JSON input with legislation that already exists in the database
        String json = "{"
                + "\"sponsoredLegislation\": ["
                + "{"
                + "\"congress\": 117,"
                + "\"introducedDate\": \"2022-06-16\","
                + "\"number\": \"4417\","
                + "\"title\": \"Patent Trial and Appeal Board Reform Act of 2022\","
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

        // Mock the Person object
        Person mockPerson = new Person();
        mockPerson.setPersonId("L000174");

        // Mock an existing SponsoredLegislation object in the database
        SponsoredLegislation existingLegislation = new SponsoredLegislation();
        existingLegislation.setSponLegId("SLEG12345");
        existingLegislation.setCongress(117);
        existingLegislation.setBillNo(4417);

        // Mock the behavior of repositories and idGenerator
        when(personRepository.findPersonByPersonId("L000174")).thenReturn(mockPerson);
        when(sponsoredLegislationRepository.findByCongressAndBillNo(117, 4417)).thenReturn(existingLegislation);

        // Call the process method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, "L000174");

        // Verify the interactions and assert the expected results
        assertEquals(1, result.size());
        SponsoredLegislation legislation = result.get(0);
        assertEquals("SLEG12345", legislation.getSponLegId());  // Ensure the ID hasn't changed
        assertEquals("Patent Trial and Appeal Board Reform Act of 2022", legislation.getLegTitle());

        // Verify that the mocks were called
        verify(personRepository, times(1)).findPersonByPersonId("L000174");
        verify(sponsoredLegislationRepository, times(1)).findByCongressAndBillNo(117, 4417);
        verify(idGenerator, never()).generateSponsLegId();  // ID generator should not be called
    }

}
