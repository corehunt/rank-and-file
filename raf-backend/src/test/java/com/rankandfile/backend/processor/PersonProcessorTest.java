package com.rankandfile.backend.processor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.Term;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonProcessorTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private PersonProcessor personProcessor;

    private JsonObject mockMemberJson;

    @BeforeEach
    public void setUp() {
        // Set up a mock JSON object representing a member
        String json = "{\n" +
                "  \"member\": {\n" +
                "    \"bioguideId\": \"A123\",\n" +
                "    \"firstName\": \"John\",\n" +
                "    \"lastName\": \"Doe\",\n" +
                "    \"addressInformation\": {\n" +
                "      \"city\": \"Washington\",\n" +
                "      \"district\": \"DC\",\n" +
                "      \"zipCode\": \"20515\",\n" +
                "      \"officeAddress\": \"1234 Longworth House Office Building\",\n" +
                "      \"phoneNumber\": \"202-225-1234\"\n" +
                "    },\n" +
                "    \"district\": 1,\n" +
                "    \"officialWebsiteUrl\": \"http://johndoe.house.gov\",\n" +
                "    \"partyHistory\": [\n" +
                "      {\n" +
                "        \"partyAbbreviation\": \"D\",\n" +
                "        \"startYear\": 2020\n" +
                "      }\n" +
                "    ],\n" +
                "    \"depiction\": {\n" +
                "      \"attribution\": \"Photo by Photographer\",\n" +
                "      \"imageUrl\": \"http://example.com/image.jpg\"\n" +
                "    },\n" +
                "    \"state\": \"California\"\n" +
                "  }\n" +
                "}";
        mockMemberJson = JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    public void testValidatePersonNewPerson() {
        String bioguideId = "A123";
        when(personRepository.findById(bioguideId)).thenReturn(Optional.empty());

        Person person = personProcessor.validatePerson(mockMemberJson.toString());

        assertNotNull(person);
        assertEquals("A123", person.getPersonId());
        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
        assertEquals("John Doe", person.getFullName());
        assertEquals("1234 Longworth House Office Building", person.getOfficeLocLine1());
        assertEquals("Washington DC, 20515", person.getOfficeLocLine2());
        assertEquals("202-225-1234", person.getPhoneNo());
        assertEquals(1, person.getCurrentDistrict());
        assertEquals("http://johndoe.house.gov", person.getWebsite());
        assertEquals("D", person.getPartyMembership());
        assertEquals(2020, person.getPartyStartYr());
        assertEquals("Photo by Photographer", person.getImgAttribution());
        assertEquals("http://example.com/image.jpg", person.getImageUrl());
        assertEquals("California", person.getState());

        // Verify interactions
        verify(personRepository, times(1)).findById(bioguideId);
    }

    @Test
    public void testValidatePersonExistingPerson() {
        String bioguideId = "A123";
        Person existingPerson = new Person();
        existingPerson.setPersonId(bioguideId);
        existingPerson.setFirstName("OldFirstName");
        existingPerson.setLastName("OldLastName");
        existingPerson.setFullName("OldFirstName OldLastName");
        existingPerson.setState("NY");

        when(personRepository.findById(bioguideId)).thenReturn(Optional.of(existingPerson));

        Person person = personProcessor.validatePerson(mockMemberJson.toString());

        assertNotNull(person);
        assertSame(existingPerson, person);
        assertEquals("A123", person.getPersonId());
        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
        assertEquals("John Doe", person.getFullName());
        assertEquals("1234 Longworth House Office Building", person.getOfficeLocLine1());
        assertEquals("Washington DC, 20515", person.getOfficeLocLine2());
        assertEquals("202-225-1234", person.getPhoneNo());
        assertEquals(1, person.getCurrentDistrict());
        assertEquals("http://johndoe.house.gov", person.getWebsite());
        assertEquals("D", person.getPartyMembership());
        assertEquals(2020, person.getPartyStartYr());
        assertEquals("Photo by Photographer", person.getImgAttribution());
        assertEquals("http://example.com/image.jpg", person.getImageUrl());
        assertEquals("California", person.getState());

        // Verify interactions
        verify(personRepository, times(1)).findById(bioguideId);
    }

    @Test
    public void testValidatePersonWithNullValues() {
        String bioguideId = "A123";
        String json = "{\n" +
                "  \"member\": {\n" +
                "    \"bioguideId\": \"A123\",\n" +
                "    \"firstName\": null,\n" +
                "    \"lastName\": null,\n" +
                "    \"addressInformation\": null,\n" +
                "    \"district\": null,\n" +
                "    \"officialWebsiteUrl\": null,\n" +
                "    \"partyHistory\": [],\n" +
                "    \"depiction\": null,\n" +
                "    \"state\": null\n" +
                "  }\n" +
                "}";
        JsonObject mockNullMemberJson = JsonParser.parseString(json).getAsJsonObject();

        when(personRepository.findById(bioguideId)).thenReturn(Optional.empty());

        Person person = personProcessor.validatePerson(mockNullMemberJson.toString());

        assertNotNull(person);
        assertEquals("A123", person.getPersonId());
        assertNull(person.getFirstName());
        assertNull(person.getLastName());
        assertNull(person.getMidName());
        assertNull(person.getFullName());
        assertNull(person.getOfficeLocLine1());
        assertNull(person.getOfficeLocLine2());
        assertNull(person.getPhoneNo());
        assertNull(person.getCurrentDistrict());
        assertNull(person.getWebsite());
        assertNull(person.getPartyMembership());
        assertNull(person.getPartyStartYr());
        assertNull(person.getImgAttribution());
        assertNull(person.getImageUrl());
        assertNull(person.getState());

        // Verify interactions
        verify(personRepository, times(1)).findById(bioguideId);
    }

    @Test
    public void testValidatePersonWithNameField() {
        String bioguideId = "H234";
        String json = "{\n" +
                "  \"member\": {\n" +
                "    \"bioguideId\": \"H234\",\n" +
                "    \"name\": \"Smith, Anna Marie\",\n" +
                "    \"state\": \"New York\"\n" +
                "  }\n" +
                "}";
        JsonObject mockNameFieldJson = JsonParser.parseString(json).getAsJsonObject();

        when(personRepository.findById(bioguideId)).thenReturn(Optional.empty());

        Person person = personProcessor.validatePerson(mockNameFieldJson.toString());

        assertNotNull(person);
        assertEquals("H234", person.getPersonId());
        assertEquals("Anna", person.getFirstName());
        assertEquals("Marie", person.getMidName());
        assertEquals("Smith", person.getLastName());
        assertEquals("Anna Marie Smith", person.getFullName());
        assertEquals("New York", person.getState());

        // Verify interactions
        verify(personRepository, times(1)).findById(bioguideId);
    }

    @Test
    public void testValidatePersonInvalidJson() {
        String invalidJson = "{ invalid json ";

        Person person = personProcessor.validatePerson(invalidJson);

        assertNull(person);

        // Verify interactions
        verify(personRepository, never()).findById(anyString());
    }

    @Test
    public void testValidatePersonWithEmptyJson() {
        String emptyJson = "";

        Person person = personProcessor.validatePerson(emptyJson);

        assertNull(person);

        // Verify interactions
        verify(personRepository, never()).findById(anyString());
    }

    @Test
    public void testValidatePersonWithSeparateNameFields() {
        String bioguideId = "C789";
        String json = "{\n" +
                "  \"member\": {\n" +
                "    \"bioguideId\": \"C789\",\n" +
                "    \"firstName\": \"Emily\",\n" +
                "    \"middleName\": \"A.\",\n" +
                "    \"lastName\": \"Clark\",\n" +
                "    \"state\": \"Florida\"\n" +
                "  }\n" +
                "}";
        JsonObject mockSeparateNameJson = JsonParser.parseString(json).getAsJsonObject();

        when(personRepository.findById(bioguideId)).thenReturn(Optional.empty());

        Person person = personProcessor.validatePerson(mockSeparateNameJson.toString());

        assertNotNull(person);
        assertEquals("C789", person.getPersonId());
        assertEquals("Emily", person.getFirstName());
        assertEquals("A.", person.getMidName());
        assertEquals("Clark", person.getLastName());
        assertEquals("Emily A. Clark", person.getFullName());
        assertEquals("Florida", person.getState());

        // Verify interactions
        verify(personRepository, times(1)).findById(bioguideId);
    }

    @Test
    public void testValidatePersonWithEmptyPartyHistory() {
        String bioguideId = "D012";
        String json = "{\n" +
                "  \"member\": {\n" +
                "    \"bioguideId\": \"D012\",\n" +
                "    \"firstName\": \"Alice\",\n" +
                "    \"lastName\": \"Smith\",\n" +
                "    \"partyHistory\": [],\n" +
                "    \"state\": \"Illinois\"\n" +
                "  }\n" +
                "}";
        JsonObject mockEmptyPartyHistoryJson = JsonParser.parseString(json).getAsJsonObject();

        when(personRepository.findById(bioguideId)).thenReturn(Optional.empty());

        Person person = personProcessor.validatePerson(mockEmptyPartyHistoryJson.toString());

        assertNotNull(person);
        assertEquals("D012", person.getPersonId());
        assertEquals("Alice", person.getFirstName());
        assertEquals("Smith", person.getLastName());
        assertEquals("Alice Smith", person.getFullName());
        assertEquals("Illinois", person.getState());

        // Party fields should remain null
        assertNull(person.getPartyMembership());
        assertNull(person.getPartyStartYr());

        // Verify interactions
        verify(personRepository, times(1)).findById(bioguideId);
    }

    @Test
    public void testValidatePersonWithMultiplePartyHistoryEntries() {
        String bioguideId = "E345";
        String json = "{\n" +
                "  \"member\": {\n" +
                "    \"bioguideId\": \"E345\",\n" +
                "    \"firstName\": \"Bob\",\n" +
                "    \"lastName\": \"Jones\",\n" +
                "    \"partyHistory\": [\n" +
                "      { \"partyAbbreviation\": \"I\", \"startYear\": 2010 },\n" +
                "      { \"partyAbbreviation\": \"D\", \"startYear\": 2015 }\n" +
                "    ],\n" +
                "    \"state\": \"Ohio\"\n" +
                "  }\n" +
                "}";
        JsonObject mockMultiplePartyHistoryJson = JsonParser.parseString(json).getAsJsonObject();

        when(personRepository.findById(bioguideId)).thenReturn(Optional.empty());

        Person person = personProcessor.validatePerson(mockMultiplePartyHistoryJson.toString());

        assertNotNull(person);
        assertEquals("E345", person.getPersonId());
        assertEquals("Bob", person.getFirstName());
        assertEquals("Jones", person.getLastName());
        assertEquals("Bob Jones", person.getFullName());
        assertEquals("Ohio", person.getState());

        // Party fields should be set to the first entry
        assertEquals("I", person.getPartyMembership());
        assertEquals(2010, person.getPartyStartYr());

        // Terms should remain empty as processor handles only the first partyHistory entry
        assertTrue(person.getTermList().isEmpty());

        // Verify interactions
        verify(personRepository, times(1)).findById(bioguideId);
    }

    @Test
    public void testValidatePersonWithTerms() {
        String bioguideId = "F678";
        Person existingPerson = new Person();
        existingPerson.setPersonId(bioguideId);
        existingPerson.setFirstName("George");
        existingPerson.setLastName("Harris");
        existingPerson.setFullName("George Harris");
        existingPerson.setState("WA");
        existingPerson.setTermList(new ArrayList<>());

        when(personRepository.findById(bioguideId)).thenReturn(Optional.of(existingPerson));

        when(idGenerator.generateTermId()).thenReturn(2001, 2002);

        String json = "{\n" +
                "  \"member\": {\n" +
                "    \"bioguideId\": \"F678\",\n" +
                "    \"firstName\": \"George\",\n" +
                "    \"lastName\": \"Harris\",\n" +
                "    \"partyHistory\": [\n" +
                "      { \"partyAbbreviation\": \"R\", \"startYear\": 2010 }\n" +
                "    ],\n" +
                "    \"state\": \"Washington\",\n" +
                "    \"terms\": [\n" +
                "      { \"chamber\": \"Senate\", \"congress\": 111, \"district\": null, \"endYear\": 2018, \"memberType\": \"Senator\", \"startYear\": 2014, \"stateCode\": \"WA\", \"stateName\": \"Washington\" },\n" +
                "      { \"chamber\": \"Senate\", \"congress\": 112, \"district\": null, \"endYear\": 2024, \"memberType\": \"Senator\", \"startYear\": 2018, \"stateCode\": \"WA\", \"stateName\": \"Washington\" }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        JsonObject mockTermsJson = JsonParser.parseString(json).getAsJsonObject();

        Person person = personProcessor.validatePerson(mockTermsJson.toString());

        assertNotNull(person);
        assertSame(existingPerson, person);
        assertEquals("F678", person.getPersonId());
        assertEquals("George", person.getFirstName());
        assertEquals("Harris", person.getLastName());
        assertEquals("George Harris", person.getFullName());
        assertEquals("Washington", person.getState());

        // Party fields should be updated
        assertEquals("R", person.getPartyMembership());
        assertEquals(2010, person.getPartyStartYr());

        // Terms should be updated
        List<Term> termList = person.getTermList();
        assertNotNull(termList);
        assertEquals(2, termList.size());

        Term term1 = termList.get(0);
        assertEquals(2001, term1.getTermId());
        assertEquals("Senate", term1.getChamber());
        assertEquals(111, term1.getCongress());
        assertNull(term1.getDistrict());
        assertEquals(2018, term1.getEndYr());
        assertEquals("Senator", term1.getMemberType());
        assertEquals(2014, term1.getStartYr());
        assertEquals("WA", term1.getStateCd());
        assertEquals("Washington", term1.getStateNm());

        Term term2 = termList.get(1);
        assertEquals(2002, term2.getTermId());
        assertEquals("Senate", term2.getChamber());
        assertEquals(112, term2.getCongress());
        assertNull(term2.getDistrict());
        assertEquals(2024, term2.getEndYr());
        assertEquals("Senator", term2.getMemberType());
        assertEquals(2018, term2.getStartYr());
        assertEquals("WA", term2.getStateCd());
        assertEquals("Washington", term2.getStateNm());

        // Verify interactions
        verify(personRepository, times(1)).findById(bioguideId);
    }

    @Test
    public void testCleanAddress() {
        String cleanedAddress = personProcessor.cleanAddress("Washington", "DC", "20515");
        assertEquals("Washington DC, 20515", cleanedAddress);
    }
}