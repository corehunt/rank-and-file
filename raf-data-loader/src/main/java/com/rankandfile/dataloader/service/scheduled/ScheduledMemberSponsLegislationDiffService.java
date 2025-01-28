package com.rankandfile.dataloader.service.scheduled;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.dataloader.entity.SponsoredLegislation;
import com.rankandfile.dataloader.processor.SponsoredLegislationProcessor;
import com.rankandfile.dataloader.repository.SponsoredLegislationRepository;
import com.rankandfile.dataloader.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScheduledMemberSponsLegislationDiffService {

    private final WebClient webClient;
    private final SponsoredLegislationProcessor sponsoredLegislationProcessor;
    private final SponsoredLegislationRepository sponsoredLegislationRepository;
    private final IdGenerator idGenerator;

    private final String FIELD_SPONSOR = "Sponsor";
    private final String FIELD_CO_SPONSOR = "Co-Sponsor";
    private final String FIELD_SPONSOR_ENDPOINT = "sponsored-legislation";
    private final String FIELD_CO_SPONSOR_ENDPOINT = "cosponsored-legislation";
    private final String FIELD_SPONSOR_LEG = "sponsoredLegislation";
    private final String FIELD_CO_SPONSOR_LEG = "cosponsoredLegislation";
    private final String FIELD_CONGRESS = "congress";
    private final String FIELD_NUMBER = "number";
    private final String FIELD_TYPE = "type";


    public ScheduledMemberSponsLegislationDiffService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            SponsoredLegislationProcessor sponsoredLegislationProcessor,
            SponsoredLegislationRepository sponsoredLegislationRepository,
            IdGenerator idGenerator) {
        this.webClient = webClient;
        this.sponsoredLegislationProcessor = sponsoredLegislationProcessor;
        this.sponsoredLegislationRepository = sponsoredLegislationRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Generic method that fetches either sponsored or cosponsored legislation from Congress.gov
     * in pages of 'limit', and stops as soon as detected already-existing data.
     *
     * @param personId    The person's ID
     * @param sponsorType "Sponsor" or "Co-Sponsor"
     * @param limit       Page size (limit per request)
     * @return A list of newly added SponsoredLegislation records
     */
    public List<SponsoredLegislation> getLegislationByPersonId(String personId, String sponsorType, int limit) {
        List<SponsoredLegislation> allSponsLegList = new ArrayList<>();
        int offset = 0;
        boolean hasMoreRecords = true;

        log.info("Starting to fetch {} Legislation for Member: {}", sponsorType, personId);

        try {
            while (hasMoreRecords) {
                log.info("[PersonId: {}, SponsorType: {}, Offset: {}] Requesting next page of legislation...", personId, sponsorType, offset);

                // 1. Pull raw JSON from either /sponsored-legislation or /cosponsored-legislation
                String response = fetchLegislation(personId, sponsorType, offset, limit);
                if (response == null || response.isEmpty()) {
                    log.warn("[PersonId: {}, SponsorType: {}, Offset: {}] Empty/null response. Stopping.", personId, sponsorType, offset);
                    break;
                }

                // 2. Check the first item in this response to see if it's already in the DB
                //    If yes, assume all older data is also in DB, so stop here.
                boolean firstItemExisting = isFirstItemExisting(response, personId, sponsorType);
                if (firstItemExisting) {
                    log.info("[PersonId: {}, SponsorType: {}, Offset: {}] First item is already in DB. Stopping early.", personId, sponsorType, offset);
                    break;
                }

                // 3. Otherwise, parse/process the full page
                log.info("[PersonId: {}, SponsorType: {}, Offset: {}] Parsing full page...",
                        personId, sponsorType, offset);
                List<SponsoredLegislation> legislationList = sponsoredLegislationProcessor.process(response, personId);
                if (legislationList.isEmpty()) {
                    log.warn("[PersonId: {}, SponsorType: {}, Offset: {}] No items parsed from JSON. Possibly end of data.", personId, sponsorType, offset);
                    break;
                }

                List<SponsoredLegislation> newItems = new ArrayList<>();
                boolean foundExistingRecord = false;

                // 4. Within the page, if we find an existing record, we stop *this page*
                for (SponsoredLegislation item : legislationList) {
                    if (alreadyExists(item)) {
                        foundExistingRecord = true;
                        log.info("[PersonId: {}, SponsorType: {}, Offset: {}] Found existing record (BillId={}, Type={}). Stopping page.", personId, sponsorType, offset, item.getBill().getBillId(), item.getSponsorType());
                        break;
                    } else {
                        newItems.add(item);
                    }
                }

                if (!newItems.isEmpty()) {
                    sponsoredLegislationRepository.saveAll(newItems);
                    allSponsLegList.addAll(newItems);
                    log.info("[PersonId: {}, SponsorType: {}, Offset: {}] Saved {} new item(s).", personId, sponsorType, offset, newItems.size());
                } else {
                    log.info("[PersonId: {}, SponsorType: {}, Offset: {}] No new items to save this page.", personId, sponsorType, offset);
                }

                // If we found an existing record, we stop for older data
                if (foundExistingRecord) {
                    log.info("[PersonId: {}, SponsorType: {}, Offset: {}] Encountered existing record. Breaking out for older data.", personId, sponsorType, offset);
                    break;
                }

                // 5. If the page wasn't full, we've likely reached the end
                if (legislationList.size() < limit) {
                    log.info("[PersonId: {}, SponsorType: {}, Offset: {}] Less than {} items returned. End of data.", personId, sponsorType, offset, limit);
                    hasMoreRecords = false;
                } else {
                    offset += limit;
                    log.info("[PersonId: {}, SponsorType: {}] Incrementing offset to {} for next page...", personId, sponsorType, offset);
                }
            }

            log.info("Completed fetching. Total newly added {} Legislation: {} for PersonId: {}", sponsorType, allSponsLegList.size(), personId);

        } catch (Exception e) {
            log.error("[PersonId: {}, SponsorType: {}] Error while processing: {}", personId, sponsorType, e.getMessage(), e);
        }

        return allSponsLegList;
    }

    /**
     * Checks if the first item in the JSON response is already in DB.
     * If yes, we stop to avoid reprocessing older data.
     */
    private boolean isFirstItemExisting(String jsonResponse, String personId, String sponsorType) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            // Depending on sponsorType, the array name differs:
            //  Sponsor => "sponsoredLegislation"
            //  Co-Sponsor => "cosponsoredLegislation"
            String jsonArrayName = (FIELD_CO_SPONSOR.equalsIgnoreCase(sponsorType))
                    ? FIELD_CO_SPONSOR_LEG
                    : FIELD_SPONSOR_LEG;

            if (!root.has(jsonArrayName)) {
                log.info("[PersonId: {}, SponsorType: {}] No '{}' array in response, skipping first-item check.", personId, sponsorType, jsonArrayName);
                return false;
            }

            JsonArray array = root.getAsJsonArray(jsonArrayName);
            if (array == null || array.isEmpty()) {
                log.info("[PersonId: {}, SponsorType: {}] '{}' array is empty, no first-item check.", personId, sponsorType, jsonArrayName);
                return false;
            }

            // Take the first item
            JsonObject firstItem = array.get(0).getAsJsonObject();
            String congressNo = getAsString(firstItem, FIELD_CONGRESS);
            String billNo = getAsString(firstItem, FIELD_NUMBER);
            String billType = getAsString(firstItem, FIELD_TYPE);

            if (congressNo == null || billNo == null || billType == null) {
                log.info("[PersonId: {}, SponsorType: {}] Missing (congress, number, type) in first item. Skipping existence check.", personId, sponsorType);
                return false;
            }

            // Build BillID
            String generatedBillId = idGenerator.generateBillId(congressNo, billType, billNo);

            // Check DB
            boolean exists = sponsoredLegislationRepository.existsByPerson_PersonIdAndBill_BillIdAndSponsorType(personId, generatedBillId, sponsorType);

            log.info("[PersonId: {}, SponsorType: {}] First item => BillId={}, existsInDB={}", personId, sponsorType, generatedBillId, exists);
            return exists;

        } catch (Exception e) {
            log.error("[PersonId: {}, SponsorType: {}] Failed to parse first item: {}", personId, sponsorType, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if a single item already exists by (personId, billId, sponsorType).
     */
    private boolean alreadyExists(SponsoredLegislation item) {
        String personId = item.getPerson().getPersonId();
        String billId   = item.getBill().getBillId();
        String stype    = item.getSponsorType();

        boolean exists = sponsoredLegislationRepository.existsByPerson_PersonIdAndBill_BillIdAndSponsorType(personId, billId, stype);

        if (exists) {
            log.info("[PersonId: {}, BillId={}, SponsorType={}] Found existing record in DB.", personId, billId, stype);
        }

        return exists;
    }

    /**
     * Makes a GET request to the correct endpoint based on sponsorType ("Sponsor" vs. "Co-Sponsor").
     */
    private String fetchLegislation(String personId, String sponsorType, int offset, int limit) {
        // Endpoint path differs for sponsor vs. co-sponsor
        String endpoint = FIELD_CO_SPONSOR.equalsIgnoreCase(sponsorType)
                ? FIELD_CO_SPONSOR_ENDPOINT
                : FIELD_SPONSOR_ENDPOINT;

        log.info("[PersonId: {}, SponsorType: {}, Offset: {}] Invoking Congress.gov at path='/member/{}/{}' (limit={}, offset={})", personId, sponsorType, offset, personId, endpoint, limit, offset);

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("member/{personId}/" + endpoint)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build(personId))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getAsString(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            return null;
        }
        return obj.get(field).getAsString();
    }
}
