package com.rankandfile.dataloader.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BillSubjectProcessor {

    private static final String FIELD_SUBJECTS = "subjects";
    private static final String FIELD_LEG_SUBJECTS = "legislativeSubjects";
    private static final String FIELD_NAME = "name";

    public BillSubjectProcessor() {};

    /**
     * Processes the JSON response to extract legislative subjects and updates the RAF_BILL table.
     *
     * @param jsonResponse The JSON string containing legislative subjects.
     * @param billId       The ID of the bill to update.
     */
    public String processLegislativeSubjects(String jsonResponse, String billId) {
        log.info("Processing legislative subjects for Bill ID: {}", billId);

        JsonObject rootObject;
        try {
            rootObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to parse JSON response for Bill ID: {}", billId, e);
            return null;
        }

        if (rootObject == null || !rootObject.has(FIELD_SUBJECTS)) {
            log.warn("No subjects found in the response for Bill ID: {}", billId);
            return null;
        }

        JsonObject subjectsObject = rootObject.getAsJsonObject(FIELD_SUBJECTS);
        if (subjectsObject == null || !subjectsObject.has(FIELD_LEG_SUBJECTS)) {
            log.warn("No legislativeSubjects found for Bill ID: {}", billId);
            return null;
        }

        JsonArray legislativeSubjectsArray = subjectsObject.getAsJsonArray(FIELD_LEG_SUBJECTS);
        if (legislativeSubjectsArray == null || legislativeSubjectsArray.isEmpty()) {
            log.warn("legislativeSubjects array is empty for Bill ID: {}", billId);
            return null;
        }

        List<String> subjects = new ArrayList<>();

        for (JsonElement element : legislativeSubjectsArray) {
            JsonObject subjectObject = element.getAsJsonObject();
            String name = subjectObject.has(FIELD_NAME) && !subjectObject.get(FIELD_NAME).isJsonNull()
                    ? subjectObject.get(FIELD_NAME).getAsString()
                    : null;
            if (name != null && !name.trim().isEmpty()) {
                subjects.add(name.trim());
            }
        }

        if (subjects.isEmpty()) {
            log.warn("No valid legislativeSubjects extracted for Bill ID: {}", billId);
            return null;
        }

        // Convert List<String> to pipe-delimited String
        return String.join("|", subjects);
    }
}
