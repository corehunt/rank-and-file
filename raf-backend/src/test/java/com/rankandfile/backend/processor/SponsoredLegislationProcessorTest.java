package com.rankandfile.backend.processor;

import com.google.gson.JsonObject;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.repository.SponsoredLegislationRepository;
import com.rankandfile.backend.util.IdGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SponsoredLegislationProcessorTest {

    @Mock
    private SponsoredLegislationRepository sponsoredLegislationRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private SponsoredLegislationProcessor sponsoredLegislationProcessor;

    @Test
    void testProcessSponsoredLegislation() {
        // Prepare test data
        String personId = "P123";
        String json = "{\n" +
                "  \"sponsoredLegislation\": [\n" +
                "    {\n" +
                "      \"congress\": 118,\n" +
                "      \"number\": \"1234\",\n" +
                "      \"type\": \"HR\",\n" +
                "      \"url\": \"https://api.congress.gov/v3/bill/118/hr/1234?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Person person = new Person();
        person.setPersonId(personId);

        when(personRepository.findPersonByPersonId(personId)).thenReturn(person);
        when(billRepository.findByCongressAndBillNoAndBillType(118, 1234, "HR")).thenReturn(null);
        when(idGenerator.generateBillId(118, "HR", 1234)).thenReturn("118-HR-1234");
        when(idGenerator.generateSponsLegId()).thenReturn("SL123");
        doNothing().when(billByCongressTypeNumberProcessor).updateBillFromJson(any(JsonObject.class), any(Bill.class));

        // Mock existing legislations
        when(sponsoredLegislationRepository.findByPersonPersonIdAndSponsorType(personId, "Sponsor"))
                .thenReturn(Collections.emptyList());

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        SponsoredLegislation legislation = result.get(0);
        assertEquals("SL123", legislation.getSponLegId());
        assertEquals(person, legislation.getPerson());
        assertEquals("Sponsor", legislation.getSponsorType());

        // Verify Bill details
        Bill bill = legislation.getBill();
        assertNotNull(bill);
        assertEquals("118-HR-1234", bill.getBillId());
        assertEquals(118, bill.getCongress());
        assertEquals(1234, bill.getBillNo());
        assertEquals("HR", bill.getBillType());

        // Verify interactions
        verify(personRepository).findPersonByPersonId(personId);
        verify(billRepository).findByCongressAndBillNoAndBillType(118, 1234, "HR");
        verify(idGenerator).generateBillId(118, "HR", 1234);
        verify(idGenerator).generateSponsLegId();
        verify(billByCongressTypeNumberProcessor).updateBillFromJson(any(JsonObject.class), any(Bill.class));
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void testProcessCosponsoredLegislation() {
        // Prepare test data
        String personId = "P123";
        String json = "{\n" +
                "  \"cosponsoredLegislation\": [\n" +
                "    {\n" +
                "      \"congress\": 118,\n" +
                "      \"number\": \"5678\",\n" +
                "      \"type\": \"S\",\n" +
                "      \"url\": \"https://api.congress.gov/v3/bill/118/s/5678?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Person person = new Person();
        person.setPersonId(personId);

        when(personRepository.findPersonByPersonId(personId)).thenReturn(person);
        when(billRepository.findByCongressAndBillNoAndBillType(118, 5678, "S")).thenReturn(null);
        when(idGenerator.generateBillId(118, "S", 5678)).thenReturn("118-S-5678");
        when(idGenerator.generateSponsLegId()).thenReturn("SL456");
        doNothing().when(billByCongressTypeNumberProcessor).updateBillFromJson(any(JsonObject.class), any(Bill.class));

        // Mock existing legislations
        when(sponsoredLegislationRepository.findByPersonPersonIdAndSponsorType(personId, "Co-Sponsor"))
                .thenReturn(Collections.emptyList());

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        SponsoredLegislation legislation = result.get(0);
        assertEquals("SL456", legislation.getSponLegId());
        assertEquals(person, legislation.getPerson());
        assertEquals("Co-Sponsor", legislation.getSponsorType());

        // Verify Bill details
        Bill bill = legislation.getBill();
        assertNotNull(bill);
        assertEquals("118-S-5678", bill.getBillId());
        assertEquals(118, bill.getCongress());
        assertEquals(5678, bill.getBillNo());
        assertEquals("S", bill.getBillType());

        // Verify interactions
        verify(personRepository).findPersonByPersonId(personId);
        verify(billRepository).findByCongressAndBillNoAndBillType(118, 5678, "S");
        verify(idGenerator).generateBillId(118, "S", 5678);
        verify(idGenerator).generateSponsLegId();
        verify(billByCongressTypeNumberProcessor).updateBillFromJson(any(JsonObject.class), any(Bill.class));
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void testProcessNullJson() {
        String personId = "P123";
        String json = null;

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify that no interactions occur
        verifyNoInteractions(personRepository, billRepository, billByCongressTypeNumberProcessor, idGenerator);
    }

    @Test
    void testProcessEmptyJson() {
        String personId = "P123";
        String json = "";

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify that no interactions occur
        verifyNoInteractions(personRepository, billRepository, billByCongressTypeNumberProcessor, idGenerator);
    }

    @Test
    void testProcessJsonWithMissingFields() {
        // Prepare test data
        String personId = "P123";
        String json = "{\n" +
                "  \"sponsoredLegislation\": [\n" +
                "    {\n" +
                "      \"congress\": 118,\n" +
                "      \"type\": \"HR\",\n" +
                "      \"url\": \"https://api.congress.gov/v3/bill/118/hr/1234?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Person person = new Person();
        person.setPersonId(personId);

        when(personRepository.findPersonByPersonId(personId)).thenReturn(person);

        // Mock existing legislations
        when(sponsoredLegislationRepository.findByPersonPersonIdAndSponsorType(personId, "Sponsor"))
                .thenReturn(Collections.emptyList());

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify interactions
        verify(personRepository).findPersonByPersonId(personId);
        verifyNoMoreInteractions(personRepository);
        verifyNoInteractions(idGenerator, billRepository, billByCongressTypeNumberProcessor);
    }

    @Test
    void testProcessSkipsAmendments() {
        // Prepare test data
        String personId = "P123";
        String json = "{\n" +
                "  \"sponsoredLegislation\": [\n" +
                "    {\n" +
                "      \"congress\": 118,\n" +
                "      \"number\": \"1234\",\n" +
                "      \"type\": \"HR\",\n" +
                "      \"url\": \"https://api.congress.gov/v3/amendment/118/hr/1234?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Person person = new Person();
        person.setPersonId(personId);

        when(personRepository.findPersonByPersonId(personId)).thenReturn(person);

        // Mock existing legislations
        when(sponsoredLegislationRepository.findByPersonPersonIdAndSponsorType(personId, "Sponsor"))
                .thenReturn(Collections.emptyList());

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify interactions
        verify(personRepository).findPersonByPersonId(personId);
        verifyNoMoreInteractions(personRepository);
        verifyNoInteractions(billRepository, billByCongressTypeNumberProcessor, idGenerator);
    }

    @Test
    void testProcessInvalidBillNumberFormat() {
        // Prepare test data
        String personId = "P123";
        String json = "{\n" +
                "  \"sponsoredLegislation\": [\n" +
                "    {\n" +
                "      \"congress\": 118,\n" +
                "      \"number\": \"12A34\",\n" +
                "      \"type\": \"HR\",\n" +
                "      \"url\": \"https://api.congress.gov/v3/bill/118/hr/12A34?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Person person = new Person();
        person.setPersonId(personId);

        when(personRepository.findPersonByPersonId(personId)).thenReturn(person);
        when(billRepository.findByCongressAndBillNoAndBillType(118, 1234, "HR")).thenReturn(null);
        when(idGenerator.generateBillId(118, "HR", 1234)).thenReturn("118-HR-1234");
        when(idGenerator.generateSponsLegId()).thenReturn("SL123");
        doNothing().when(billByCongressTypeNumberProcessor).updateBillFromJson(any(JsonObject.class), any(Bill.class));

        // Mock existing legislations
        when(sponsoredLegislationRepository.findByPersonPersonIdAndSponsorType(personId, "Sponsor"))
                .thenReturn(Collections.emptyList());

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        SponsoredLegislation legislation = result.get(0);
        assertEquals("SL123", legislation.getSponLegId());
        assertEquals(person, legislation.getPerson());
        assertEquals("Sponsor", legislation.getSponsorType());

        // Verify Bill details
        Bill bill = legislation.getBill();
        assertNotNull(bill);
        assertEquals("118-HR-1234", bill.getBillId());
        assertEquals(118, bill.getCongress());
        assertEquals(1234, bill.getBillNo());
        assertEquals("HR", bill.getBillType());

        // Verify interactions
        verify(personRepository).findPersonByPersonId(personId);
        verify(billRepository).findByCongressAndBillNoAndBillType(118, 1234, "HR");
        verify(idGenerator).generateBillId(118, "HR", 1234);
        verify(idGenerator).generateSponsLegId();
        verify(billByCongressTypeNumberProcessor).updateBillFromJson(any(JsonObject.class), any(Bill.class));
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void testProcessPersonNotFound() {
        // Prepare test data
        String personId = "P123";
        String json = "{\n" +
                "  \"sponsoredLegislation\": [\n" +
                "    {\n" +
                "      \"congress\": 118,\n" +
                "      \"number\": \"1234\",\n" +
                "      \"type\": \"HR\",\n" +
                "      \"url\": \"https://api.congress.gov/v3/bill/118/hr/1234?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(personRepository.findPersonByPersonId(personId)).thenReturn(null);

        // Execute the method and expect exception
        assertThrows(EntityNotFoundException.class, () -> {
            sponsoredLegislationProcessor.process(json, personId);
        });

        // Verify interactions
        verify(personRepository).findPersonByPersonId(personId);
        verifyNoMoreInteractions(personRepository);
        verifyNoInteractions(billRepository, billByCongressTypeNumberProcessor, idGenerator);
    }

    @Test
    void testProcessBillAlreadyExists() {
        // Prepare test data
        String personId = "P123";
        String json = "{\n" +
                "  \"sponsoredLegislation\": [\n" +
                "    {\n" +
                "      \"congress\": 118,\n" +
                "      \"number\": \"1234\",\n" +
                "      \"type\": \"HR\",\n" +
                "      \"url\": \"https://api.congress.gov/v3/bill/118/hr/1234?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Person person = new Person();
        person.setPersonId(personId);

        Bill bill = new Bill();
        bill.setBillId("BILL1234");
        bill.setCongress(118);
        bill.setBillNo(1234);
        bill.setBillType("HR");

        when(personRepository.findPersonByPersonId(personId)).thenReturn(person);
        when(billRepository.findByCongressAndBillNoAndBillType(118, 1234, "HR")).thenReturn(bill);
        when(idGenerator.generateSponsLegId()).thenReturn("SL123");

        // Mock existing legislations
        when(sponsoredLegislationRepository.findByPersonPersonIdAndSponsorType(personId, "Sponsor"))
                .thenReturn(Collections.emptyList());

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        SponsoredLegislation legislation = result.get(0);
        assertEquals("SL123", legislation.getSponLegId());
        assertEquals(person, legislation.getPerson());
        assertEquals("Sponsor", legislation.getSponsorType());
        assertEquals(bill, legislation.getBill());

        // Verify interactions
        verify(personRepository).findPersonByPersonId(personId);
        verify(billRepository).findByCongressAndBillNoAndBillType(118, 1234, "HR");
        verify(idGenerator).generateSponsLegId();
        verifyNoInteractions(billByCongressTypeNumberProcessor);
    }

    @Test
    void testProcessSponsoredLegislationAlreadyExists() {
        // Prepare test data
        String personId = "P123";
        String sponsorType = "Sponsor";
        String json = "{\n" +
                "  \"sponsoredLegislation\": [\n" +
                "    {\n" +
                "      \"congress\": 118,\n" +
                "      \"number\": \"1234\",\n" +
                "      \"type\": \"HR\",\n" +
                "      \"url\": \"https://api.congress.gov/v3/bill/118/hr/1234?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Person person = new Person();
        person.setPersonId(personId);

        Bill bill = new Bill();
        bill.setBillId("BILL1234");
        bill.setCongress(118);
        bill.setBillNo(1234);
        bill.setBillType("HR");

        SponsoredLegislation existingLegislation = new SponsoredLegislation();
        existingLegislation.setSponLegId("SL123");
        existingLegislation.setPerson(person);
        existingLegislation.setBill(bill);
        existingLegislation.setSponsorType(sponsorType);

        when(personRepository.findPersonByPersonId(personId)).thenReturn(person);
        when(billRepository.findByCongressAndBillNoAndBillType(118, 1234, "HR")).thenReturn(bill);

        // Mock existing legislations
        when(sponsoredLegislationRepository.findByPersonPersonIdAndSponsorType(personId, sponsorType))
                .thenReturn(Collections.singletonList(existingLegislation));

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        SponsoredLegislation legislation = result.get(0);
        assertEquals("SL123", legislation.getSponLegId());
        assertEquals(person, legislation.getPerson());
        assertEquals("Sponsor", legislation.getSponsorType());
        assertEquals(bill, legislation.getBill());

        // Verify interactions
        verify(personRepository).findPersonByPersonId(personId);
        verify(billRepository).findByCongressAndBillNoAndBillType(118, 1234, "HR");
        verifyNoInteractions(billByCongressTypeNumberProcessor, idGenerator);
    }

    @Test
    void testProcessEmptyLegislationArray() {
        // Prepare test data
        String personId = "P123";
        String json = "{\n" +
                "  \"sponsoredLegislation\": []\n" +
                "}";

        // Execute the method
        List<SponsoredLegislation> result = sponsoredLegislationProcessor.process(json, personId);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify interactions
        verifyNoInteractions(billRepository, billByCongressTypeNumberProcessor, idGenerator, personRepository);
    }
}
