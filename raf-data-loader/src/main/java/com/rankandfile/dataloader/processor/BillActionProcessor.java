package com.rankandfile.dataloader.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.dataloader.entity.Action;
import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.repository.ActionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BillActionProcessor {

    private static final String FIELD_ACTIONS = "actions";
    private static final String FIELD_ACTION_CODE = "actionCode";
    private static final String FIELD_ACTION_DATE = "actionDate";
    private static final String FIELD_SOURCE_SYSTEM = "sourceSystem";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_COMMITTEE = "committees";
    private static final String FIELD_SYSTEM_CODE = "systemCode";

    private final ActionRepository actionRepository;

    public BillActionProcessor(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    /**
     * Processes the JSON string containing actions and associates them with a bill.
     * Uses an in-memory map to check if an action (with identical fields) already exists:
     * - If yes, updates it.
     * - If no, creates a new one.
     *
     * This avoids additional database calls during processing.
     *
     * @param json The JSON string containing actions.
     * @param bill The bill entity to associate the actions with.
     * @return A list of Action entities (updated or newly created).
     */
    public List<Action> processActionList(String json, Bill bill) {
        log.info("Starting processing Actions for Bill #: {}", bill.getBillNo());

        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();
        if (rootObject == null || !rootObject.has(FIELD_ACTIONS)) {
            log.warn("No actions found for Bill #: {}", bill.getBillNo());
            return Collections.emptyList();
        }

        JsonArray billActionArray = rootObject.getAsJsonArray(FIELD_ACTIONS);
        if (billActionArray == null || billActionArray.isEmpty()) {
            log.warn("Actions array is empty for Bill #: {}", bill.getBillNo());
            return Collections.emptyList();
        }

        // Load existing actions from the database once
        List<Action> existingActions = actionRepository.findByBillBillId(bill.getBillId());

        // Create a map of existing actions keyed by a stable combined key of their fields
        Map<String, Action> actionMap = existingActions.stream()
                .collect(Collectors.toMap(
                        this::generateKeyFromAction,
                        action -> action
                ));

        List<Action> resultActions = new ArrayList<>(existingActions);

        for (JsonElement element : billActionArray) {
            JsonObject actionObject = element.getAsJsonObject();

            // Generate key for the incoming action data
            String actionKey = generateKeyFromJson(actionObject);

            // Attempt to find an existing action in memory
            Action action = actionMap.get(actionKey);

            if (action == null) {
                // Create new action
                action = new Action();
                action.setBill(bill);
                log.info("Creating new Action for Bill #: {} with key: {}", bill.getBillNo(), actionKey);
                applyActionProperties(actionObject, action);
                // Add to map and results
                actionMap.put(actionKey, action);
                resultActions.add(action);
            } else {
                // Update existing action fields
                log.info("Updating existing action for Bill #: {} with key: {}", bill.getBillNo(), actionKey);
                applyActionProperties(actionObject, action);
            }
        }

        log.info("Completed processing Actions for Bill #: {}", bill.getBillNo());
        return resultActions;
    }

    private void applyActionProperties(JsonObject actionObject, Action action) {
        // Action Code
        String actionCode = getAsString(actionObject, FIELD_ACTION_CODE);
        action.setActionCode(actionCode);

        // Action Date
        LocalDate actionDate = parseActionDate(getAsString(actionObject, FIELD_ACTION_DATE));
        action.setActionDate(actionDate);

        // Source System
        if (actionObject.has(FIELD_SOURCE_SYSTEM) && !actionObject.get(FIELD_SOURCE_SYSTEM).isJsonNull()) {
            JsonObject sourceSystemObject = actionObject.getAsJsonObject(FIELD_SOURCE_SYSTEM);
            Integer sourceSystemCode = getAsInteger(sourceSystemObject, FIELD_CODE);
            action.setSourceSystemCode(sourceSystemCode);

            String sourceSystemName = getAsString(sourceSystemObject, FIELD_NAME);
            action.setSourceSystemName(sourceSystemName);
        } else {
            action.setSourceSystemCode(null);
            action.setSourceSystemName(null);
        }

        // Action Text
        String actionText = getAsString(actionObject, FIELD_TEXT);
        action.setActionText(actionText);

        // Action Type
        String actionType = getAsString(actionObject, FIELD_TYPE);
        action.setActionType(actionType);

        // Committees
        updateActionCommittees(actionObject, action);
    }

    private void updateActionCommittees(JsonObject actionObject, Action action) {
        JsonArray committeeArray = actionObject.getAsJsonArray(FIELD_COMMITTEE);
        if (committeeArray != null && !committeeArray.isEmpty()) {
            String committeeRef = parseCommitteesToRef(committeeArray);
            action.setCommitteeRef(committeeRef);
        } else {
            action.setCommitteeRef(null);
        }
    }

    private String parseCommitteesToRef(JsonArray committeeArray) {
        List<String> committeeEntries = new ArrayList<>();
        for (JsonElement committeeElem : committeeArray) {
            JsonObject committeeObj = committeeElem.getAsJsonObject();
            String systemCode = getAsString(committeeObj, FIELD_SYSTEM_CODE);
            String name = getAsString(committeeObj, FIELD_NAME);

            if (systemCode != null && name != null) {
                committeeEntries.add(systemCode + ":" + name);
            }
        }
        return String.join(";", committeeEntries);
    }

    private LocalDate parseActionDate(String dateString) {
        if (dateString != null) {
            try {
                return LocalDate.parse(dateString);
            } catch (DateTimeParseException e) {
                log.error("Invalid date format for actionDate: {}", dateString, e);
            }
        }
        return null;
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsString() : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        String value = getAsString(obj, field);
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            log.error("Invalid number format for field {}: {}", field, value, e);
            return null;
        }
    }

    /**
     * Generates a key for an existing Action. We incorporate multiple fields to reduce collisions.
     */
    private String generateKeyFromAction(Action action) {
        String code = action.getActionCode() != null ? action.getActionCode() : "unknownCode";
        String date = action.getActionDate() != null ? action.getActionDate().toString() : "unknownDate";
        String text = action.getActionText() != null ? String.valueOf(action.getActionText().hashCode()) : "unknownText";
        String type = action.getActionType() != null ? action.getActionType() : "unknownType";
        String sourceName = action.getSourceSystemName() != null ? action.getSourceSystemName() : "unknownSource";
        String committees = action.getCommitteeRef() != null ? String.valueOf(action.getCommitteeRef().hashCode()) : "unknownComm";

        // Combine all to form a stable key
        return code + "-" + date + "-" + text + "-" + type + "-" + sourceName + "-" + committees;
    }

    /**
     * Generates a key for a JSON action before creating/updating. Uses the same fields as generateKeyFromAction.
     */
    private String generateKeyFromJson(JsonObject actionObject) {
        String code = getAsString(actionObject, FIELD_ACTION_CODE);
        String date = getAsString(actionObject, FIELD_ACTION_DATE);
        String text = getAsString(actionObject, FIELD_TEXT);
        String type = getAsString(actionObject, FIELD_TYPE);

        String codeKey = code != null ? code : "unknownCode";
        String dateKey = date != null ? date : "unknownDate";
        String textKey = text != null ? String.valueOf(text.hashCode()) : "unknownText";
        String typeKey = type != null ? type : "unknownType";

        // Source System Name
        String sourceSystemName = "unknownSource";
        if (actionObject.has(FIELD_SOURCE_SYSTEM) && !actionObject.get(FIELD_SOURCE_SYSTEM).isJsonNull()) {
            JsonObject sourceSystemObj = actionObject.getAsJsonObject(FIELD_SOURCE_SYSTEM);
            String sName = getAsString(sourceSystemObj, FIELD_NAME);
            if (sName != null) {
                sourceSystemName = sName;
            }
        }

        // Committees
        JsonArray committeeArray = actionObject.getAsJsonArray(FIELD_COMMITTEE);
        String committees = "unknownComm";
        if (committeeArray != null && committeeArray.size() > 0) {
            String cRef = parseCommitteesToRef(committeeArray);
            committees = String.valueOf(cRef.hashCode());
        }

        return codeKey + "-" + dateKey + "-" + textKey + "-" + typeKey + "-" + sourceSystemName + "-" + committees;
    }
}
