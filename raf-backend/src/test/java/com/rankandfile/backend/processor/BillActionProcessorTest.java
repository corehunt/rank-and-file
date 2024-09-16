package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Action;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.ActionRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillActionProcessorTest {

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ActionRepository actionRepository;

    @InjectMocks
    private BillActionProcessor processor;


    @Test
    void testBillActionProcessor() {
        // Mock IdGenerator
        when(idGenerator.generateActionId()).thenReturn("ACTION_ID_1", "ACTION_ID_2", "ACTION_ID_3");

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
                "    ]\n" +
                "}";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");
        mockBill.setBillNo(4926);
        mockBill.setBillTitle("Respect for Child Survivors Act");
        mockBill.setCongress(117);
        mockBill.setBillType("S");
        mockBill.setOriginChamberCd("S");
        mockBill.setOriginChamber("Senate");

        // Mock ActionRepository to return no existing actions
        when(actionRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(3, actionList.size());

        // Assertions for first action
        Action action1 = actionList.get(0);
        assertEquals("ACTION_ID_1", action1.getActionId());
        assertEquals("E40000", action1.getActionCode());
        assertEquals(LocalDate.of(2023, 1, 5), action1.getActionDate());
        assertEquals(9, action1.getSourceSystemCode());
        assertEquals("Library of Congress", action1.getSourceSystemName());
        assertEquals("Became Public Law No: 117-354.", action1.getActionText());
        assertEquals("President", action1.getActionType());

        // Assertions for second action
        Action action2 = actionList.get(1);
        assertEquals("ACTION_ID_2", action2.getActionId());
        assertEquals("H37300", action2.getActionCode());
        assertEquals(LocalDate.of(2022, 12, 21), action2.getActionDate());
        assertEquals(2, action2.getSourceSystemCode());
        assertEquals("House floor actions", action2.getSourceSystemName());
        assertEquals("On motion to suspend the rules and pass the bill Agreed to by the Yeas and Nays: (2/3 required): 385 - 28 (Roll no. 534). (text: CR H9927-9929)", action2.getActionText());
        assertEquals("Floor", action2.getActionType());

        // Assertions for third action
        Action action3 = actionList.get(2);
        assertEquals("ACTION_ID_3", action3.getActionId());
        assertNull(action3.getActionCode());
        assertEquals(LocalDate.of(2022, 12, 13), action3.getActionDate());
        assertNull(action3.getSourceSystemCode());
        assertEquals("Senate", action3.getSourceSystemName());
        assertEquals("Passed Senate with an amendment by Unanimous Consent. (text of amendment in the nature of a substitute: CR S7146-7147)", action3.getActionText());
        assertEquals("Floor", action3.getActionType());
    }

    @Test
    void testBillActionProcessorWithEmptyActions() {
        String json = "{ \"actions\": [] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(0, actionList.size());
    }

    @Test
    void testBillActionProcessorWithMissingFields() {
        String json = "{ \"actions\": [ { \"text\": \"Passed Senate without a vote.\", \"type\": \"Floor\" } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        when(actionRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());
        when(idGenerator.generateActionId()).thenReturn("ACTION_ID_1");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(1, actionList.size());

        Action action = actionList.get(0);
        assertEquals("ACTION_ID_1", action.getActionId());
        assertNull(action.getActionCode());
        assertNull(action.getActionDate());
        assertNull(action.getSourceSystemCode());
        assertNull(action.getSourceSystemName());
        assertEquals("Passed Senate without a vote.", action.getActionText());
        assertEquals("Floor", action.getActionType());
    }

    @Test
    void testBillActionProcessorWithNullFields() {
        String json = "{ \"actions\": [ { \"actionCode\": null, \"actionDate\": \"2023-01-05\", \"sourceSystem\": { \"code\": null, \"name\": null }, \"text\": \"Became Public Law No: 117-354.\", \"type\": null } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        when(actionRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());
        when(idGenerator.generateActionId()).thenReturn("ACTION_ID_1");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(1, actionList.size());

        Action action = actionList.get(0);
        assertEquals("ACTION_ID_1", action.getActionId());
        assertNull(action.getActionCode());
        assertEquals(LocalDate.of(2023, 1, 5), action.getActionDate());
        assertNull(action.getSourceSystemCode());
        assertNull(action.getSourceSystemName());
        assertEquals("Became Public Law No: 117-354.", action.getActionText());
        assertNull(action.getActionType());
    }

    @Test
    void testBillActionProcessorWithUnexpectedJsonStructure() {
        String json = "{ \"actions\": [ { \"unexpectedField\": \"unexpectedValue\", \"anotherField\": {} } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        when(actionRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());
        when(idGenerator.generateActionId()).thenReturn("ACTION_ID_1");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(1, actionList.size());

        Action action = actionList.get(0);
        assertEquals("ACTION_ID_1", action.getActionId());
        assertNull(action.getActionCode());
        assertNull(action.getActionDate());
        assertNull(action.getSourceSystemCode());
        assertNull(action.getSourceSystemName());
        assertNull(action.getActionText());
        assertNull(action.getActionType());
    }

    @Test
    void testBillActionProcessorWithSameDateActions() {
        String json = "{ \"actions\": [ { \"actionCode\": \"A10000\", \"actionDate\": \"2023-01-05\", \"sourceSystem\": { \"code\": 9, \"name\": \"Library of Congress\" }, \"text\": \"Text for Action 1.\", \"type\": \"President\" }, { \"actionCode\": \"B20000\", \"actionDate\": \"2023-01-05\", \"sourceSystem\": { \"code\": 9, \"name\": \"Library of Congress\" }, \"text\": \"Text for Action 2.\", \"type\": \"Floor\" } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        when(actionRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());
        when(idGenerator.generateActionId()).thenReturn("ACTION_ID_1", "ACTION_ID_2");

        List<Action> actionList = processor.processActionList(json, mockBill);
        assertEquals(2, actionList.size());

        Action action1 = actionList.stream().filter(a -> a.getActionCode().equals("A10000")).findFirst().orElse(null);
        assertNotNull(action1);
        assertEquals("ACTION_ID_1", action1.getActionId());
        assertEquals(LocalDate.of(2023, 1, 5), action1.getActionDate());
        assertEquals("Text for Action 1.", action1.getActionText());
        assertEquals("President", action1.getActionType());

        Action action2 = actionList.stream().filter(a -> a.getActionCode().equals("B20000")).findFirst().orElse(null);
        assertNotNull(action2);
        assertEquals("ACTION_ID_2", action2.getActionId());
        assertEquals(LocalDate.of(2023, 1, 5), action2.getActionDate());
        assertEquals("Text for Action 2.", action2.getActionText());
        assertEquals("Floor", action2.getActionType());
    }

    @Test
    void testProcessActionListWithExistingActions() {
        String json = "{ \"actions\": [ { \"actionCode\": \"E40000\", \"actionDate\": \"2023-01-05\", \"text\": \"Became Public Law No: 117-354.\", \"type\": \"President\" }, { \"actionCode\": \"H37300\", \"actionDate\": \"2022-12-21\", \"text\": \"On motion to suspend the rules and pass the bill.\", \"type\": \"Floor\" } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        // Existing action
        Action existingAction = new Action();
        existingAction.setActionId("EXISTING_ACTION_ID");
        existingAction.setBill(mockBill);
        existingAction.setActionCode("E40000");
        existingAction.setActionDate(LocalDate.of(2023, 1, 5));
        existingAction.setActionText("Became Public Law No: 117-354.");
        existingAction.setActionType("President");

        when(actionRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.singletonList(existingAction));
        when(idGenerator.generateActionId()).thenReturn("NEW_ACTION_ID");

        List<Action> actionList = processor.processActionList(json, mockBill);

        assertEquals(2, actionList.size());

        // Existing action should be reused
        Action action1 = actionList.stream().filter(a -> a.getActionCode().equals("E40000")).findFirst().orElse(null);
        assertNotNull(action1);
        assertEquals("EXISTING_ACTION_ID", action1.getActionId());

        // New action should be created
        Action action2 = actionList.stream().filter(a -> a.getActionCode().equals("H37300")).findFirst().orElse(null);
        assertNotNull(action2);
        assertEquals("NEW_ACTION_ID", action2.getActionId());
    }

    @Test
    void testProcessActionListWithAllExistingActions() {
        String json = "{ \"actions\": [ { \"actionCode\": \"E40000\", \"actionDate\": \"2023-01-05\", \"text\": \"Became Public Law No: 117-354.\", \"type\": \"President\" } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        // Existing action
        Action existingAction = new Action();
        existingAction.setActionId("EXISTING_ACTION_ID");
        existingAction.setBill(mockBill);
        existingAction.setActionCode("E40000");
        existingAction.setActionDate(LocalDate.of(2023, 1, 5));
        existingAction.setActionText("Became Public Law No: 117-354.");
        existingAction.setActionType("President");

        when(actionRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.singletonList(existingAction));

        List<Action> actionList = processor.processActionList(json, mockBill);

        assertEquals(1, actionList.size());
        Action action = actionList.get(0);
        assertEquals("EXISTING_ACTION_ID", action.getActionId());

        // Verify that idGenerator.generateActionId() was not called
        verify(idGenerator, never()).generateActionId();
    }

    @Test
    void testProcessActionListWithInvalidDate() {
        String json = "{ \"actions\": [ { \"actionCode\": \"E40000\", \"actionDate\": \"invalid-date\", \"text\": \"Became Public Law No: 117-354.\", \"type\": \"President\" } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("117-4926");

        when(actionRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());
        when(idGenerator.generateActionId()).thenReturn("NEW_ACTION_ID");

        List<Action> actionList = processor.processActionList(json, mockBill);

        assertEquals(1, actionList.size());
        Action action = actionList.get(0);
        assertEquals("NEW_ACTION_ID", action.getActionId());
        assertEquals("E40000", action.getActionCode());
        assertNull(action.getActionDate());
        assertEquals("Became Public Law No: 117-354.", action.getActionText());
        assertEquals("President", action.getActionType());
    }
}
