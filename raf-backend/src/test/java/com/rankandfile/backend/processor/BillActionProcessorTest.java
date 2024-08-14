package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Action;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BillActionProcessorTest {

    private BillActionProcessor processor;
    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() throws Exception {
        idGenerator = mock(IdGenerator.class);
        processor = new BillActionProcessor(idGenerator);
    }

    @Test
    void testBillActionProcessor() {
        when(idGenerator.generateActionId()).thenAnswer(invocation -> {
            Random random = new Random();
            int randomNumber = random.nextInt(9000) + 1000; // Generates a random number between 1000 and 9999
            return "AB" + randomNumber;
        });

        String json = "{\n" +
                "    \"actions\": [\n" +
                "        {\n" +
                "            \"actionCode\": \"E40000\",\n" +
                "            \"actionDate\": \"2023-01-05\",\n" +
                "            \"sourceSystem\": {\n" +
                "                \"code\": 9,\n" +
                "                \"name\": \"Library of Congress\"\n" +
                "            },\n" +
                "            \"text\": \"Became Public Law No: 117-354.\",\n" +
                "            \"type\": \"President\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"actionCode\": \"H37300\",\n" +
                "            \"actionDate\": \"2022-12-21\",\n" +
                "            \"actionTime\": \"22:10:06\",\n" +
                "            \"recordedVotes\": [\n" +
                "                {\n" +
                "                    \"chamber\": \"House\",\n" +
                "                    \"congress\": 117,\n" +
                "                    \"date\": \"2022-12-22T03:10:06Z\",\n" +
                "                    \"rollNumber\": 534,\n" +
                "                    \"sessionNumber\": 2,\n" +
                "                    \"url\": \"https://clerk.house.gov/evs/2022/roll534.xml\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"sourceSystem\": {\n" +
                "                \"code\": 2,\n" +
                "                \"name\": \"House floor actions\"\n" +
                "            },\n" +
                "            \"text\": \"On motion to suspend the rules and pass the bill Agreed to by the Yeas and Nays: (2/3 required): 385 - 28 (Roll no. 534). (text: CR H9927-9929)\",\n" +
                "            \"type\": \"Floor\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"actionDate\": \"2022-12-13\",\n" +
                "            \"sourceSystem\": {\n" +
                "                \"name\": \"Senate\"\n" +
                "            },\n" +
                "            \"text\": \"Passed Senate with an amendment by Unanimous Consent. (text of amendment in the nature of a substitute: CR S7146-7147)\",\n" +
                "            \"type\": \"Floor\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"pagination\": {\n" +
                "        \"count\": 3,\n" +
                "        \"next\": \"https://api.congress.gov/v3/bill/117/s/4926/actions?offset=20&limit=20&format=json\"\n" +
                "    },\n" +
                "    \"request\": {\n" +
                "        \"billNumber\": \"4926\",\n" +
                "        \"billType\": \"s\",\n" +
                "        \"billUrl\": \"https://api.congress.gov/v3/bill/117/s/4926?format=json\",\n" +
                "        \"congress\": \"117\",\n" +
                "        \"contentType\": \"application/json\",\n" +
                "        \"format\": \"json\"\n" +
                "    }\n" +
                "}";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");
        mockBill.setBillNo(4926);
        mockBill.setBillTitle("Respect for Child Survivors Act");
        mockBill.setCongress(117);
        mockBill.setBillType("S");
        mockBill.setOriginChamberCd("S");
        mockBill.setOriginChamber("Senate");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(3, actionList.size());

        assertEquals("E40000", actionList.get(0).getActionCode());
        assertEquals(LocalDate.of(2023, 1, 5), actionList.get(0).getActionDate());
        assertEquals(9, actionList.get(0).getSourceSystemCode());
        assertEquals("Library of Congress", actionList.get(0).getSourceSystemName());
        assertEquals("Became Public Law No: 117-354.", actionList.get(0).getActionText());
        assertEquals("President", actionList.get(0).getActionType());

        assertEquals("H37300", actionList.get(1).getActionCode());
        assertEquals(LocalDate.of(2022, 12, 21), actionList.get(1).getActionDate());
        assertEquals(2, actionList.get(1).getSourceSystemCode());
        assertEquals("House floor actions", actionList.get(1).getSourceSystemName());
        assertEquals("On motion to suspend the rules and pass the bill Agreed to by the Yeas and Nays: (2/3 required): 385 - 28 (Roll no. 534). (text: CR H9927-9929)", actionList.get(1).getActionText());
        assertEquals("Floor", actionList.get(1).getActionType());

        assertNull(actionList.get(2).getActionCode());
        assertEquals(LocalDate.of(2022, 12, 13), actionList.get(2).getActionDate());
        assertNull(actionList.get(2).getSourceSystemCode());
        assertEquals("Senate", actionList.get(2).getSourceSystemName());
        assertEquals("Passed Senate with an amendment by Unanimous Consent. (text of amendment in the nature of a substitute: CR S7146-7147)", actionList.get(2).getActionText());
        assertEquals("Floor", actionList.get(2).getActionType());

    }

    @Test
    void testBillActionProcessorWithEmptyActions() {
        String json = "{\n" +
                "    \"actions\": [],\n" +
                "    \"pagination\": {\n" +
                "        \"count\": 0,\n" +
                "        \"next\": \"\"\n" +
                "    }\n" +
                "}";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(0, actionList.size());
    }

    @Test
    void testBillActionProcessorWithMissingFields() {
        String json = "{\n" +
                "    \"actions\": [\n" +
                "        {\n" +
                "            \"text\": \"Passed Senate without a vote.\",\n" +
                "            \"type\": \"Floor\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(1, actionList.size());
        assertNull(actionList.get(0).getActionCode());
        assertNull(actionList.get(0).getActionDate());
        assertNull(actionList.get(0).getSourceSystemCode());
        assertNull(actionList.get(0).getSourceSystemName());
        assertEquals("Passed Senate without a vote.", actionList.get(0).getActionText());
        assertEquals("Floor", actionList.get(0).getActionType());
    }

    @Test
    void testBillActionProcessorWithNullFields() {
        String json = "{\n" +
                "    \"actions\": [\n" +
                "        {\n" +
                "            \"actionCode\": null,\n" +
                "            \"actionDate\": \"2023-01-05\",\n" +
                "            \"sourceSystem\": {\n" +
                "                \"code\": null,\n" +
                "                \"name\": null\n" +
                "            },\n" +
                "            \"text\": \"Became Public Law No: 117-354.\",\n" +
                "            \"type\": null\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(1, actionList.size());
        assertNull(actionList.get(0).getActionCode());
        assertEquals(LocalDate.of(2023, 1, 5), actionList.get(0).getActionDate());
        assertNull(actionList.get(0).getSourceSystemCode());
        assertNull(actionList.get(0).getSourceSystemName());
        assertEquals("Became Public Law No: 117-354.", actionList.get(0).getActionText());
        assertNull(actionList.get(0).getActionType());
    }

    @Test
    void testBillActionProcessorWithUnexpectedJsonStructure() {
        String json = "{\n" +
                "    \"actions\": [\n" +
                "        {\n" +
                "            \"unexpectedField\": \"unexpectedValue\",\n" +
                "            \"anotherField\": {}\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(1, actionList.size());
        assertNull(actionList.get(0).getActionCode());
        assertNull(actionList.get(0).getActionDate());
        assertNull(actionList.get(0).getSourceSystemCode());
        assertNull(actionList.get(0).getSourceSystemName());
        assertNull(actionList.get(0).getActionText());
        assertNull(actionList.get(0).getActionType());
    }

    @Test
    void testBillActionProcessorWithSameDateActions() {
        String json = "{\n" +
                "    \"actions\": [\n" +
                "        {\n" +
                "            \"actionCode\": \"A10000\",\n" +
                "            \"actionDate\": \"2023-01-05\",\n" +
                "            \"sourceSystem\": {\n" +
                "                \"code\": 9,\n" +
                "                \"name\": \"Library of Congress\"\n" +
                "            },\n" +
                "            \"text\": \"Text for Action 1.\",\n" +
                "            \"type\": \"President\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"actionCode\": \"B20000\",\n" +
                "            \"actionDate\": \"2023-01-05\",\n" +
                "            \"sourceSystem\": {\n" +
                "                \"code\": 9,\n" +
                "                \"name\": \"Library of Congress\"\n" +
                "            },\n" +
                "            \"text\": \"Text for Action 2.\",\n" +
                "            \"type\": \"Floor\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(2, actionList.size());

        assertEquals("A10000", actionList.get(0).getActionCode());
        assertEquals(LocalDate.of(2023, 1, 5), actionList.get(0).getActionDate());
        assertEquals("Text for Action 1.", actionList.get(0).getActionText());
        assertEquals("President", actionList.get(0).getActionType());

        assertEquals("B20000", actionList.get(1).getActionCode());
        assertEquals(LocalDate.of(2023, 1, 5), actionList.get(1).getActionDate());
        assertEquals("Text for Action 2.", actionList.get(1).getActionText());
        assertEquals("Floor", actionList.get(1).getActionType());
    }


}
