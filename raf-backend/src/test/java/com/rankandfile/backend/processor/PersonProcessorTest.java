package com.rankandfile.backend.processor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.Term;
import com.rankandfile.backend.entity.domain.StateDomain;
import com.rankandfile.backend.repository.StateRepository;
import com.rankandfile.backend.util.IdGenerator;
import com.rankandfile.backend.util.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonProcessorTest {

    @Mock
    private StateRepository stateRepository;

    @Mock
    private Supplier personSupplier;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private PersonProcessor personProcessor;

    @InjectMocks
    private CongressMemberProcessor congressMemberProcessor;

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
    public void testValidatePerson() {
        Person mockPerson = new Person();
        when(personSupplier.findOrCreatePerson(anyString())).thenReturn(mockPerson);

        Person person = personProcessor.validatePerson(mockMemberJson.toString());

        assertEquals("A123", person.getPersonId());
        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
        assertEquals("1234 Longworth House Office Building", person.getOfficeLocLine1());
        assertEquals("Washington, DC 20515", person.getOfficeLocLine2());
        assertEquals("202-225-1234", person.getPhoneNo());
        assertEquals(1, person.getCurrentDistrict());
        assertEquals("http://johndoe.house.gov", person.getWebsite());
        assertEquals("D", person.getPartyMembership());
        assertEquals(2020, person.getPartyStartYr());
        assertEquals("Photo by Photographer", person.getImgAttribution());
        assertEquals("http://example.com/image.jpg", person.getImageUrl());
        assertEquals("California", person.getState());
    }

    @Test
    public void testValidatePersonWithNullValues() {
        Person mockPerson = new Person();
        when(personSupplier.findOrCreatePerson(anyString())).thenReturn(mockPerson);

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

        Person person = personProcessor.validatePerson(mockNullMemberJson.toString());

        assertEquals("A123", person.getPersonId());
        assertNull(person.getFirstName());
        assertNull(person.getLastName());
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
    }

    @Test
    public void testValidatePersonWithNameField() {
        String json = "{\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"bioguideId\": \"B001320\",\n" +
                "      \"depiction\": {\n" +
                "        \"attribution\": \"Image courtesy of the Senator's office\",\n" +
                "        \"imageUrl\": \"https://www.congress.gov/img/member/b001320_200.jpg\"\n" +
                "      },\n" +
                "      \"name\": \"Butler, Laphonza R.\",\n" +
                "      \"partyName\": \"Democratic\",\n" +
                "      \"state\": \"California\",\n" +
                "      \"terms\": {\n" +
                "        \"item\": [\n" +
                "          {\n" +
                "            \"chamber\": \"Senate\",\n" +
                "            \"startYear\": 2023\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"updateDate\": \"2024-04-09T15:54:25Z\",\n" +
                "      \"url\": \"http://api.congress.gov/v3/member/B001320?format=json\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"bioguideId\": \"A000376\",\n" +
                "      \"depiction\": {\n" +
                "        \"attribution\": \"Image courtesy of the Member\",\n" +
                "        \"imageUrl\": \"https://www.congress.gov/img/member/a000376_200.jpg\"\n" +
                "      },\n" +
                "      \"district\": 32,\n" +
                "      \"name\": \"Allred, Colin Z.\",\n" +
                "      \"partyName\": \"Democratic\",\n" +
                "      \"state\": \"Texas\",\n" +
                "      \"terms\": {\n" +
                "        \"item\": [\n" +
                "          {\n" +
                "            \"chamber\": \"House of Representatives\",\n" +
                "            \"startYear\": 2019\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"updateDate\": \"2024-04-09T13:26:21Z\",\n" +
                "      \"url\": \"http://api.congress.gov/v3/member/A000376?format=json\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        JsonObject mockNameMemberJson = JsonParser.parseString(json).getAsJsonObject();

        List<Person> personList = congressMemberProcessor.processMembers(mockNameMemberJson.toString());

        assertEquals("B001320", personList.get(0).getPersonId());
        assertEquals("Laphonza", personList.get(0).getFirstName());
        assertEquals("Butler", personList.get(0).getLastName());
        assertEquals("A000376", personList.get(1).getPersonId());
        assertEquals("Colin", personList.get(1).getFirstName());
        assertEquals("Allred", personList.get(1).getLastName());
    }

    @Test
    public void testValidatePersonWithCongressTerms() {
        Person mockPerson = new Person();
        when(personSupplier.findOrCreatePerson(anyString())).thenReturn(mockPerson);
        when(idGenerator.generateTermId()).thenReturn(anyInt());


        String json = "{\n" +
                "  \"member\": {\n" +
                "    \"addressInformation\": {\n" +
                "      \"city\": \"Washington\",\n" +
                "      \"district\": \"DC\",\n" +
                "      \"officeAddress\": \"2354 Rayburn House Office Building\",\n" +
                "      \"phoneNumber\": \"(202) 225-6116\",\n" +
                "      \"zipCode\": 20515\n" +
                "    },\n" +
                "    \"bioguideId\": \"P000597\",\n" +
                "    \"birthYear\": \"1955\",\n" +
                "    \"cosponsoredLegislation\": {\n" +
                "      \"count\": 4212,\n" +
                "      \"url\": \"https://api.congress.gov/v3/member/P000597/cosponsored-legislation\"\n" +
                "    },\n" +
                "    \"currentMember\": true,\n" +
                "    \"depiction\": {\n" +
                "      \"attribution\": \"Image courtesy of the Member\",\n" +
                "      \"imageUrl\": \"https://www.congress.gov/img/member/p000597_200.jpg\"\n" +
                "    },\n" +
                "    \"directOrderName\": \"Chellie Pingree\",\n" +
                "    \"district\": 1,\n" +
                "    \"firstName\": \"Chellie\",\n" +
                "    \"honorificName\": \"Ms.\",\n" +
                "    \"invertedOrderName\": \"Pingree, Chellie\",\n" +
                "    \"lastName\": \"Pingree\",\n" +
                "    \"officialWebsiteUrl\": \"https://pingree.house.gov/\",\n" +
                "    \"partyHistory\": [\n" +
                "      {\n" +
                "        \"partyAbbreviation\": \"D\",\n" +
                "        \"partyName\": \"Democratic\",\n" +
                "        \"startYear\": 2009\n" +
                "      }\n" +
                "    ],\n" +
                "    \"sponsoredLegislation\": {\n" +
                "      \"count\": 155,\n" +
                "      \"url\": \"https://api.congress.gov/v3/member/P000597/sponsored-legislation\"\n" +
                "    },\n" +
                "    \"state\": \"Maine\",\n" +
                "    \"terms\": [\n" +
                "      {\n" +
                "        \"chamber\": \"House of Representatives\",\n" +
                "        \"congress\": 111,\n" +
                "        \"district\": 1,\n" +
                "        \"endYear\": 2011,\n" +
                "        \"memberType\": \"Representative\",\n" +
                "        \"startYear\": 2009,\n" +
                "        \"stateCode\": \"ME\",\n" +
                "        \"stateName\": \"Maine\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"chamber\": \"House of Representatives\",\n" +
                "        \"congress\": 112,\n" +
                "        \"district\": 1,\n" +
                "        \"endYear\": 2013,\n" +
                "        \"memberType\": \"Representative\",\n" +
                "        \"startYear\": 2011,\n" +
                "        \"stateCode\": \"ME\",\n" +
                "        \"stateName\": \"Maine\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"updateDate\": \"2024-06-08T18:40:18Z\"\n" +
                "  },\n" +
                "  \"request\": {\n" +
                "    \"bioguideId\": \"p000597\",\n" +
                "    \"contentType\": \"application/json\",\n" +
                "    \"format\": \"json\"\n" +
                "  }\n" +
                "}";

        JsonObject mockNullMemberJson = JsonParser.parseString(json).getAsJsonObject();

        Person person = personProcessor.validatePerson(mockNullMemberJson.toString());
        assertEquals("P000597", person.getPersonId());

        List<Term> termList = person.getTermList();

        assertEquals(2, termList.size());
        assertEquals("House of Representatives", termList.get(0).getChamber());
        assertEquals(111, termList.get(0).getCongress());
        assertEquals(1, termList.get(0).getDistrict());
        assertEquals(2011, termList.get(0).getEndYr());
        assertEquals("Representative", termList.get(0).getMemberType());
        assertEquals(2009, termList.get(0).getStartYr());
        assertEquals("ME", termList.get(0).getStateCd());
        assertEquals("Maine", termList.get(0).getStateNm());

        assertEquals("House of Representatives", termList.get(1).getChamber());
        assertEquals(112, termList.get(1).getCongress());
        assertEquals(1, termList.get(1).getDistrict());
        assertEquals(2013, termList.get(1).getEndYr());
        assertEquals("Representative", termList.get(1).getMemberType());
        assertEquals(2011, termList.get(1).getStartYr());
        assertEquals("ME", termList.get(1).getStateCd());
        assertEquals("Maine", termList.get(1).getStateNm());
    }

    @Test
    public void testCleanAddress() {
        String cleanedAddress = personProcessor.cleanAddress("1234 Longworth House Office Building", "Washington", "20515");
        assertEquals("1234 Longworth House Office Building, Washington 20515", cleanedAddress);
    }

    @Test
    public void testGetStateAbbrByFullName() {
        // Create and set up a StateDomain object
        StateDomain stateDomain = new StateDomain();
        stateDomain.setStateAbbr("CA");
        stateDomain.setStateNm("California");

        when(stateRepository.findByStateNm("California")).thenReturn(Optional.of(stateDomain));

        String stateAbbr = personProcessor.getStateAbbrByFullName("California");
        assertEquals("CA", stateAbbr);
    }

    public JsonObject prepareNullJson() {
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
                "    \"state\": \"California\"\n" +
                "  }\n" +
                "}";

        mockMemberJson = JsonParser.parseString(json).getAsJsonObject();
        return mockMemberJson;
    }
}