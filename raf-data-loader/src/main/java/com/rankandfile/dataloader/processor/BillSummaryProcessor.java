package com.rankandfile.dataloader.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Document.OutputSettings;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class BillSummaryProcessor {

    private static final String FIELD_SUMMARIES = "summaries";
    private static final String FIELD_ACTION_DATE = "actionDate";
    private static final String FIELD_ACTION_DESC = "actionDesc";
    private static final String FIELD_TEXT = "text";

    private final BillRepository billRepository;

    public BillSummaryProcessor(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * Processes the JSON response containing summaries and updates the Bill entity.
     *
     * @param json The JSON response as a String.
     * @param bill The Bill entity to update.
     */
    public Bill processBillSummaryResponse(String json, Bill bill) {
        log.info("Starting processing summary for Bill #: {}, Congress #: {}", bill.getBillNo(), bill.getCongress());

        // Check that the root has a summaries array
        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();
        if (rootObject == null || !rootObject.has(FIELD_SUMMARIES)) {
            log.warn("No summaries found for Bill #: {}, returning bill", bill.getBillNo());
            return bill;
        }

        // Check that the array isn't empty
        JsonArray summariesArray = rootObject.getAsJsonArray(FIELD_SUMMARIES);
        if (summariesArray == null || summariesArray.isEmpty()) {
            log.warn("Summaries array is empty for Bill #: {}, returning bill", bill.getBillNo());
            return bill;
        }

        // Create a list of summaries if there is more than 1
        List<JsonObject> summaries = new ArrayList<>();
        for(JsonElement summary : summariesArray) {
            summaries.add(summary.getAsJsonObject());
        }

        // Stream the list of JsonObjects to obtain the obj with the most recent action date
        Optional<JsonObject> streamedSummaryObject = summaries.stream()
                .max(Comparator.comparing(summary -> {
                    String dateString = getAsString(summary, FIELD_ACTION_DATE);
                    return LocalDate.parse(dateString);
                }));

        // Assign the most recent obj
        JsonObject latestSummaryObject = streamedSummaryObject.orElse(null);

        if (latestSummaryObject == null) {
            log.warn("No valid summaries found after filtering for Bill #: {}, returning bill", bill.getBillNo());
            return bill;
        }

        // Extract required fields from the latest summary
        String actionDateStr = getAsString(latestSummaryObject, FIELD_ACTION_DATE);
        String actionDesc = getAsString(latestSummaryObject, FIELD_ACTION_DESC);
        String textHtml = getAsString(latestSummaryObject, FIELD_TEXT);

        // Process the summary text to remove HTML tags but preserve formatting
        String processedText = htmlToPlainText(textHtml);

        // Update the Bill entity with summary details
        if (actionDateStr != null) {
            try {
                bill.setSummaryActionDt(LocalDate.parse(actionDateStr));
            } catch (DateTimeParseException e) {
                log.error("Invalid date format for summaryActionDt: {}", actionDateStr, e);
                bill.setSummaryActionDt(null);
            }
        }
        bill.setSummaryActionDesc(actionDesc);
        bill.setSummaryTxt(processedText);

        // Save the updated Bill entity
        log.info("Successfully updated Bill #: {} with the latest summary", bill.getBillNo());

        return bill;
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    private String htmlToPlainText(String html) {
        if (html == null) {
            return null;
        }

        // Replace <p> and <br> tags with line breaks
        html = html.replaceAll("(?i)<br */?>", "\n");
        html = html.replaceAll("(?i)</p>", "\n");
        html = html.replaceAll("(?i)<p>", "");

        // Parse the HTML to remove remaining tags
        Document document = Jsoup.parse(html);
        document.outputSettings(new OutputSettings().prettyPrint(false));
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().charset("UTF-8");
        String text = document.wholeText();
        return text.trim();
    }
}
