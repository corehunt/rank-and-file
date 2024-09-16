package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Action;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.ActionRepository;
import com.rankandfile.backend.util.IdGenerator;
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

    private final IdGenerator idGenerator;
    private final ActionRepository actionRepository;

    public BillActionProcessor(IdGenerator idGenerator, ActionRepository actionRepository) {
        this.idGenerator = idGenerator;
        this.actionRepository = actionRepository;
    }

    /**
     * Processes the JSON string containing actions and associates them with a bill.
     *
     * @param json The JSON string containing actions.
     * @param bill The bill entity to associate the actions with.
     * @return A list of Action entities.
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

        // Retrieve existing actions from the database
        List<Action> existingActions = actionRepository.findByBillBillId(bill.getBillId());

        // Create a map of existing actions for quick lookup
        Map<String, Action> existingActionMap = existingActions.stream()
                .collect(Collectors.toMap(
                        this::generateKey,
                        action -> action
                ));

        List<Action> actions = new ArrayList<>();

        for (JsonElement element : billActionArray) {
            JsonObject actionObject = element.getAsJsonObject();

            // Generate key for the action
            String key = generateKey(actionObject);

            Action action = existingActionMap.get(key);

            if (action == null) {
                // New action
                action = new Action();
                action.setActionId(idGenerator.generateActionId());
                action.setBill(bill);
                log.info("Creating new Action with key: {}", key);

                // Extract and set action properties
                extractActionFromJson(actionObject, action);

                // Add the new action to the list
                actions.add(action);
            } else {
                // Existing action, add to the list and move on
                log.info("Action already exists with ID: {}", action.getActionId());

                // Add the existing action to the list
                actions.add(action);

                // Do not reprocess the action
            }
        }

        log.info("Completed processing Actions for Bill #: {}", bill.getBillNo());

        return actions;
    }

    private void extractActionFromJson(JsonObject actionObject, Action action) {
        // Set Action Code
        String actionCode = getAsString(actionObject, FIELD_ACTION_CODE);
        action.setActionCode(actionCode);

        // Set Action Date
        String actionDateString = getAsString(actionObject, FIELD_ACTION_DATE);
        if (actionDateString != null) {
            try {
                LocalDate actionDate = LocalDate.parse(actionDateString);
                action.setActionDate(actionDate);
            } catch (DateTimeParseException e) {
                log.error("Invalid date format for actionDate: {}", actionDateString, e);
                action.setActionDate(null);
            }
        }

        // Process Source System
        if (actionObject.has(FIELD_SOURCE_SYSTEM) && !actionObject.get(FIELD_SOURCE_SYSTEM).isJsonNull()) {
            JsonObject sourceSystemObject = actionObject.getAsJsonObject(FIELD_SOURCE_SYSTEM);
            Integer sourceSystemCode = getAsInteger(sourceSystemObject, FIELD_CODE);
            action.setSourceSystemCode(sourceSystemCode);

            String sourceSystemName = getAsString(sourceSystemObject, FIELD_NAME);
            action.setSourceSystemName(sourceSystemName);
        }

        // Set Action Text
        String actionText = getAsString(actionObject, FIELD_TEXT);
        action.setActionText(actionText);

        // Set Action Type
        String actionType = getAsString(actionObject, FIELD_TYPE);
        action.setActionType(actionType);

        // TODO: Implement committee logic into its own table when applicable
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
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
     * Generates a unique key for an action based on its attributes.
     *
     * @param action The Action entity.
     * @return A unique key as a String.
     */
    private String generateKey(Action action) {
        String actionCode = action.getActionCode() != null ? action.getActionCode() : "unknownActionCode";
        String actionDate = action.getActionDate() != null ? action.getActionDate().toString() : "unknownActionDate";
        String actionText = action.getActionText() != null ? String.valueOf(action.getActionText().hashCode()) : "unknownActionText";
        return actionCode + "-" + actionDate + "-" + actionText;
    }

    /**
     * Generates a unique key for an action based on its JSON representation.
     *
     * @param actionObject The JSON object representing the action.
     * @return A unique key as a String.
     */
    private String generateKey(JsonObject actionObject) {
        String actionCode = getAsString(actionObject, FIELD_ACTION_CODE);
        String actionDate = getAsString(actionObject, FIELD_ACTION_DATE);
        String actionText = getAsString(actionObject, FIELD_TEXT);

        String actionCodeKey = actionCode != null ? actionCode : "unknownActionCode";
        String actionDateKey = actionDate != null ? actionDate : "unknownActionDate";
        String actionTextKey = actionText != null ? String.valueOf(actionText.hashCode()) : "unknownActionText";

        return actionCodeKey + "-" + actionDateKey + "-" + actionTextKey;
    }
}
