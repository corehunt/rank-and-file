package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.entity.Text;
import com.rankandfile.backend.repository.TextRepository;
import com.rankandfile.backend.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BillTextProcessor {

    private static final String FIELD_TEXT_VERSIONS = "textVersions";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_FORMATS = "formats";
    private static final String FIELD_FORMAT_TYPE = "type";
    private static final String FIELD_URL = "url";

    private final IdGenerator idGenerator;
    private final TextRepository textRepository;

    public BillTextProcessor(IdGenerator idGenerator, TextRepository textRepository) {
        this.idGenerator = idGenerator;
        this.textRepository = textRepository;
    }

    /**
     * Processes the JSON response containing bill texts and associates them with a bill.
     *
     * @param json The JSON string containing bill texts.
     * @param bill The bill entity to associate the texts with.
     */
    public List<Text> processBillTextResponse(String json, Bill bill) {
        log.info("Starting processing Texts for Bill #: {}", bill.getBillNo());

        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();
        if (rootObject == null || !rootObject.has(FIELD_TEXT_VERSIONS)) {
            log.warn("No textVersions found for Bill #: {}", bill.getBillNo());
            return Collections.emptyList();
        }

        JsonArray textVersionsArray = rootObject.getAsJsonArray(FIELD_TEXT_VERSIONS);
        if (textVersionsArray == null || textVersionsArray.isEmpty()) {
            log.warn("textVersions array is empty for Bill #: {}", bill.getBillNo());
            return Collections.emptyList();
        }

        // Retrieve existing texts from the database
        List<Text> existingTexts = textRepository.findByBillBillId(bill.getBillId());

        // Create a map of existing texts for quick lookup
        Map<String, Text> existingTextMap = existingTexts.stream()
                .collect(Collectors.toMap(
                        this::generateKey,
                        text -> text
                ));

        List<Text> texts = new ArrayList<>();

        for (JsonElement element : textVersionsArray) {
            JsonObject textVersionObject = element.getAsJsonObject();

            // Generate key for the text
            String key = generateKey(textVersionObject);

            Text text = existingTextMap.get(key);

            if (text == null) {
                // New text
                text = new Text();
                text.setTextId(idGenerator.generateTextId());
                text.setBill(bill);
                log.debug("Creating new Text with key: {}", key);

                // Extract and set text properties
                extractTextFromJson(textVersionObject, text);

                // Add the new text to the list
                texts.add(text);
            } else {
                // Existing text, add to the list and move on
                log.info("Text already exists with ID: {}", text.getTextId());

                // Add the existing text to the list
                texts.add(text);

                // Do not reprocess the text
            }
        }

        log.info("Completed processing Texts for Bill #: {}", bill.getBillNo());

        return texts;
    }

    private void extractTextFromJson(JsonObject textVersionObject, Text text) {
        // Extract Version Date
        String dateStr = getAsString(textVersionObject, FIELD_DATE);
        if (dateStr != null) {
            try {
                LocalDate versionDate = LocalDate.parse(dateStr.substring(0, 10)); // Truncate time if present
                text.setVersionDate(versionDate);
            } catch (DateTimeParseException e) {
                log.error("Invalid date format for versionDate: {}", dateStr, e);
                text.setVersionDate(null);
            }
        }

        // Set Version Type
        String type = getAsString(textVersionObject, FIELD_TYPE);
        text.setVersionType(type);

        // Initialize URLs
        String formattedTextUrl = null;
        String pdfUrl = null;
        String xmlUrl = null;

        // Process formats
        JsonArray formatsArray = textVersionObject.getAsJsonArray(FIELD_FORMATS);
        if (formatsArray != null) {
            for (JsonElement formatElement : formatsArray) {
                JsonObject formatObject = formatElement.getAsJsonObject();
                String formatType = getAsString(formatObject, FIELD_FORMAT_TYPE);
                String url = getAsString(formatObject, FIELD_URL);

                if ("Formatted Text".equalsIgnoreCase(formatType)) {
                    formattedTextUrl = url;
                } else if ("PDF".equalsIgnoreCase(formatType)) {
                    pdfUrl = url;
                } else if ("Formatted XML".equalsIgnoreCase(formatType)) {
                    xmlUrl = url;
                }
            }
        }

        // Set URLs
        text.setFormattedTextUrl(formattedTextUrl);
        text.setPdfUrl(pdfUrl);
        text.setXmlUrl(xmlUrl);
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    /**
     * Generates a unique key for a text based on its attributes.
     *
     * @param text The Text entity.
     * @return A unique key as a String.
     */
    private String generateKey(Text text) {
        String versionType = text.getVersionType() != null ? text.getVersionType() : "unknownVersionType";
        String versionDate = text.getVersionDate() != null ? text.getVersionDate().toString() : "unknownVersionDate";
        return versionType + "-" + versionDate;
    }

    /**
     * Generates a unique key for a text based on its JSON representation.
     *
     * @param textVersionObject The JSON object representing the text version.
     * @return A unique key as a String.
     */
    private String generateKey(JsonObject textVersionObject) {
        String versionType = getAsString(textVersionObject, FIELD_TYPE);
        String dateStr = getAsString(textVersionObject, FIELD_DATE);
        String versionDateKey;

        if (dateStr != null) {
            try {
                // Parse the date string to LocalDateTime, then extract the date
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                LocalDate versionDate = dateTime.toLocalDate();
                versionDateKey = versionDate.toString();
            } catch (DateTimeParseException e) {
                log.error("Invalid date format in JSON for key generation: {}", dateStr, e);
                versionDateKey = "unknownVersionDate";
            }
        } else {
            versionDateKey = "unknownVersionDate";
        }

        String versionTypeKey = versionType != null ? versionType : "unknownVersionType";
        return versionTypeKey + "-" + versionDateKey;
    }

}
