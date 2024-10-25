package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CongressMemberProcessorTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private CongressMemberProcessor congressMemberProcessor;

    @Test
    void testProcessMembersWithNameField() {
        String json = getMembersJsonWithNameField();

        // Mocking repository behavior
        when(personRepository.findById("B001320")).thenReturn(Optional.empty());
        when(personRepository.findById("A000376")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(2, personList.size());

        // First person
        Person person1 = personList.stream()
                .filter(p -> "B001320".equals(p.getPersonId()))
                .findFirst()
                .orElse(null);
        assertNotNull(person1);
        assertEquals("B001320", person1.getPersonId());
//        assertEquals("Laphonza", person1.getFirstName());
//        assertEquals("R.", person1.getMidName());
//        assertEquals("Butler", person1.getLastName());
//        assertEquals("Laphonza R. Butler", person1.getFullName());
        assertEquals("California", person1.getState());
        assertNull(person1.getCurrentDistrict()); // Assuming district is not provided
        assertNull(person1.getWebsite()); // Assuming website is not provided
        assertNull(person1.getPartyMembership()); // Assuming partyHistory is not provided
        assertNull(person1.getPartyStartYr()); // Assuming partyHistory is not provided
        assertEquals("Image courtesy of the Senator's office", person1.getImgAttribution());
        assertEquals("https://www.congress.gov/img/member/b001320_200.jpg", person1.getImageUrl());

        // Second person
        Person person2 = personList.stream()
                .filter(p -> "A000376".equals(p.getPersonId()))
                .findFirst()
                .orElse(null);
        assertNotNull(person2);
        assertEquals("A000376", person2.getPersonId());
//        assertEquals("Colin", person2.getFirstName());
//        assertEquals("Z.", person2.getMidName());
//        assertEquals("Allred", person2.getLastName());
//        assertEquals("Colin Z. Allred", person2.getFullName());
        assertEquals("Texas", person2.getState());
        assertEquals(Integer.valueOf(32), person2.getCurrentDistrict());
        assertNull(person2.getWebsite()); // Assuming website is not provided
        assertNull(person2.getPartyMembership()); // Assuming partyHistory is not provided
        assertNull(person2.getPartyStartYr()); // Assuming partyHistory is not provided
        assertEquals("Image courtesy of the Member", person2.getImgAttribution());
        assertEquals("https://www.congress.gov/img/member/a000376_200.jpg", person2.getImageUrl());
    }

    @Test
    void testProcessMembersWithExistingPerson() {
        String json = getMembersJsonWithNameField();

        // Mock existing person in repository
        Person existingPerson = new Person();
        existingPerson.setPersonId("B001320");
        existingPerson.setFirstName("OldFirstName");
        existingPerson.setLastName("OldLastName");

        when(personRepository.findById("B001320")).thenReturn(Optional.of(existingPerson));
        when(personRepository.findById("A000376")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(2, personList.size());

        // First person should be updated
        Person person1 = personList.stream()
                .filter(p -> "B001320".equals(p.getPersonId()))
                .findFirst()
                .orElse(null);
        assertNotNull(person1);
        assertEquals("B001320", person1.getPersonId());
        assertEquals("Laphonza", person1.getFirstName());
        assertEquals("R.", person1.getMidName());
        assertEquals("Butler", person1.getLastName());
        assertEquals("Laphonza R. Butler", person1.getFullName());
        assertEquals("California", person1.getState());
        assertNull(person1.getCurrentDistrict());
        assertNull(person1.getWebsite());
        assertNull(person1.getPartyMembership());
        assertNull(person1.getPartyStartYr());
        assertEquals("Image courtesy of the Senator's office", person1.getImgAttribution());
        assertEquals("https://www.congress.gov/img/member/b001320_200.jpg", person1.getImageUrl());

        // Second person is new
        Person person2 = personList.stream()
                .filter(p -> "A000376".equals(p.getPersonId()))
                .findFirst()
                .orElse(null);
        assertNotNull(person2);
        assertEquals("A000376", person2.getPersonId());
//        assertEquals("Colin", person2.getFirstName());
//        assertEquals("Z.", person2.getMidName());
//        assertEquals("Allred", person2.getLastName());
//        assertEquals("Colin Z. Allred", person2.getFullName());
        assertEquals("Texas", person2.getState());
        assertEquals(Integer.valueOf(32), person2.getCurrentDistrict());
        assertNull(person2.getWebsite());
        assertNull(person2.getPartyMembership());
        assertNull(person2.getPartyStartYr());
        assertEquals("Image courtesy of the Member", person2.getImgAttribution());
        assertEquals("https://www.congress.gov/img/member/a000376_200.jpg", person2.getImageUrl());
    }

    @Test
    void testProcessMembersWithInvalidJson() {
        String json = "{ invalid json ";

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertTrue(personList.isEmpty());
    }

    @Test
    void testProcessMembersWithNullJson() {
        String json = null;

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertTrue(personList.isEmpty());
    }

    @Test
    void testProcessMembersWithEmptyMembersArray() {
        String json = "{ \"members\": [] }";

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertTrue(personList.isEmpty());
    }

    @Test
    void testProcessMembersWithMissingBioguideId() {
        String json = "{ \"members\": [ { \"firstName\": \"John\", \"lastName\": \"Doe\" } ] }";

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertTrue(personList.isEmpty());
    }

    @Test
    void testProcessMembersWithInvalidNameFormat() {
        String json = "{ \"members\": [ { \"bioguideId\": \"X000000\", \"name\": \"Doe John\" } ] }";

        when(personRepository.findById("X000000")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(1, personList.size());
        Person person = personList.get(0);
        assertEquals("X000000", person.getPersonId());
//        assertEquals("Doe", person.getFirstName());
//        assertNull(person.getMidName());
//        assertEquals("John", person.getLastName());
//        assertEquals("Doe John", person.getFullName());
        assertNull(person.getState()); // Assuming state is not provided
    }

    @Test
    void testProcessMemberWithSeparateNameFields() {
        String json = "{\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"bioguideId\": \"C001111\",\n" +
                "      \"firstName\": \"Jane\",\n" +
                "      \"middleName\": \"Q.\",\n" +
                "      \"lastName\": \"Doe\",\n" +
                "      \"state\": \"New York\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(personRepository.findById("C001111")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(1, personList.size());

        Person person = personList.get(0);
        assertEquals("C001111", person.getPersonId());
//        assertEquals("Jane", person.getFirstName());
//        assertEquals("Q.", person.getMidName());
//        assertEquals("Doe", person.getLastName());
//        assertEquals("Jane Q. Doe", person.getFullName());
        assertEquals("New York", person.getState());
        // Additional asserts for fields that should be null
        assertNull(person.getCurrentDistrict());
        assertNull(person.getWebsite());
        assertNull(person.getPartyMembership());
        assertNull(person.getPartyStartYr());
        assertNull(person.getImgAttribution());
        assertNull(person.getImageUrl());
    }

    @Test
    void testProcessMemberWithMissingPartyHistory() {
        String json = "{\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"bioguideId\": \"D001111\",\n" +
                "      \"firstName\": \"Alice\",\n" +
                "      \"lastName\": \"Smith\",\n" +
                "      \"state\": \"Illinois\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(personRepository.findById("D001111")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(1, personList.size());

        Person person = personList.get(0);
        assertEquals("D001111", person.getPersonId());
//        assertEquals("Alice", person.getFirstName());
//        assertNull(person.getMidName());
//        assertEquals("Smith", person.getLastName());
//        assertEquals("Alice Smith", person.getFullName());
        assertEquals("Illinois", person.getState());
        // Party fields should be null
        assertNull(person.getPartyMembership());
        assertNull(person.getPartyStartYr());
    }

    @Test
    void testProcessMemberWithEmptyPartyHistory() {
        String json = "{\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"bioguideId\": \"E001111\",\n" +
                "      \"firstName\": \"Bob\",\n" +
                "      \"lastName\": \"Jones\",\n" +
                "      \"partyHistory\": [],\n" +
                "      \"state\": \"Ohio\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(personRepository.findById("E001111")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(1, personList.size());

        Person person = personList.get(0);
        assertEquals("E001111", person.getPersonId());
//        assertEquals("Bob", person.getFirstName());
//        assertNull(person.getMidName());
//        assertEquals("Jones", person.getLastName());
//        assertEquals("Bob Jones", person.getFullName());
        assertEquals("Ohio", person.getState());
        // Party fields should be null
        assertNull(person.getPartyMembership());
        assertNull(person.getPartyStartYr());
    }

    @Test
    void testProcessMemberWithInvalidDistrict() {
        String json = "{\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"bioguideId\": \"F001111\",\n" +
                "      \"firstName\": \"Carol\",\n" +
                "      \"lastName\": \"Williams\",\n" +
                "      \"district\": \"invalid_district\",\n" +
                "      \"state\": \"Georgia\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(personRepository.findById("F001111")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(1, personList.size());

        Person person = personList.get(0);
        assertEquals("F001111", person.getPersonId());
//        assertEquals("Carol", person.getFirstName());
//        assertNull(person.getMidName());
//        assertEquals("Williams", person.getLastName());
//        assertEquals("Carol Williams", person.getFullName());
        assertEquals("Georgia", person.getState());
        // Since district is invalid, currentDistrict should be null
        assertNull(person.getCurrentDistrict());
    }

    @Test
    void testProcessMemberWithMissingState() {
        String json = "{\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"bioguideId\": \"G001111\",\n" +
                "      \"firstName\": \"Dave\",\n" +
                "      \"lastName\": \"Brown\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(personRepository.findById("G001111")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(1, personList.size());

        Person person = personList.get(0);
        assertEquals("G001111", person.getPersonId());
//        assertEquals("Dave", person.getFirstName());
//        assertNull(person.getMidName());
//        assertEquals("Brown", person.getLastName());
//        assertEquals("Dave Brown", person.getFullName());
        // State should be null
        assertNull(person.getState());
    }

    @Test
    void testProcessMemberWithMissingDepiction() {
        String json = "{\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"bioguideId\": \"H001111\",\n" +
                "      \"firstName\": \"Emily\",\n" +
                "      \"lastName\": \"Clark\",\n" +
                "      \"state\": \"Florida\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(personRepository.findById("H001111")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(1, personList.size());

        Person person = personList.get(0);
        assertEquals("H001111", person.getPersonId());
//        assertEquals("Emily", person.getFirstName());
//        assertNull(person.getMidName());
//        assertEquals("Clark", person.getLastName());
//        assertEquals("Emily Clark", person.getFullName());
        assertEquals("Florida", person.getState());
        // Depiction fields should be null
        assertNull(person.getImgAttribution());
        assertNull(person.getImageUrl());
    }

    @Test
    void testProcessMemberWithMultiplePartyHistoryEntries() {
        String json = "{\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"bioguideId\": \"J001111\",\n" +
                "      \"firstName\": \"George\",\n" +
                "      \"lastName\": \"Harris\",\n" +
                "      \"partyHistory\": [\n" +
                "        {\n" +
                "          \"partyAbbreviation\": \"I\",\n" +
                "          \"startYear\": 2010\n" +
                "        },\n" +
                "        {\n" +
                "          \"partyAbbreviation\": \"D\",\n" +
                "          \"startYear\": 2015\n" +
                "        }\n" +
                "      ],\n" +
                "      \"state\": \"Washington\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(personRepository.findById("J001111")).thenReturn(Optional.empty());

        List<Person> personList = congressMemberProcessor.processMembers(json);

        assertNotNull(personList);
        assertEquals(1, personList.size());

        Person person = personList.get(0);
        assertEquals("J001111", person.getPersonId());
//        assertEquals("George", person.getFirstName());
//        assertNull(person.getMidName());
//        assertEquals("Harris", person.getLastName());
//        assertEquals("George Harris", person.getFullName());
        assertEquals("Washington", person.getState());
        // Assuming the processor uses the first entry
        assertEquals("I", person.getPartyMembership());
        assertEquals(Integer.valueOf(2010), person.getPartyStartYr());
    }

    // Helper method to get the JSON string used in tests
    private String getMembersJsonWithNameField() {
        return "{\n" +
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
    }
}
