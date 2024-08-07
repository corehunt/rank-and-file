package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.BillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BillByCongressTypeNumberProcessorTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor;

    @Test
    public void testProcessBillWithAllFields() {
        String json = "{\n" +
                "    \"bill\": {\n" +
                "        \"actions\": {\n" +
                "            \"count\": 3,\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/actions?format=json\"\n" +
                "        },\n" +
                "        \"committees\": {\n" +
                "            \"count\": 1,\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/committees?format=json\"\n" +
                "        },\n" +
                "        \"congress\": 118,\n" +
                "        \"constitutionalAuthorityStatementText\": \"<pre>\\n[Congressional Record Volume 170, Number 5 (Wednesday, January 10, 2024)]\\n[House]\\nFrom the Congressional Record Online through the Government Publishing Office [<a href=\\\"https://www.gpo.gov\\\">www.gpo.gov</a>]\\nBy Ms. PINGREE:\\nH.R. 6937.\\nCongress has the power to enact this legislation pursuant\\nto the following:\\nArticle I\\nThe single subject of this legislation is:\\nData\\n[Page H52]\\n</pre>\",\n" +
                "        \"cosponsors\": {\n" +
                "            \"count\": 7,\n" +
                "            \"countIncludingWithdrawnCosponsors\": 7,\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/cosponsors?format=json\"\n" +
                "        },\n" +
                "        \"introducedDate\": \"2024-01-10\",\n" +
                "        \"latestAction\": {\n" +
                "            \"actionDate\": \"2024-01-10\",\n" +
                "            \"text\": \"Referred to the House Committee on Agriculture.\"\n" +
                "        },\n" +
                "        \"number\": \"6937\",\n" +
                "        \"originChamber\": \"House\",\n" +
                "        \"originChamberCode\": \"H\",\n" +
                "        \"policyArea\": {\n" +
                "            \"name\": \"Agriculture and Food\"\n" +
                "        },\n" +
                "        \"relatedBills\": {\n" +
                "            \"count\": 1,\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/relatedbills?format=json\"\n" +
                "        },\n" +
                "        \"sponsors\": [\n" +
                "            {\n" +
                "                \"bioguideId\": \"P000597\",\n" +
                "                \"district\": 1,\n" +
                "                \"firstName\": \"Chellie\",\n" +
                "                \"fullName\": \"Rep. Pingree, Chellie [D-ME-1]\",\n" +
                "                \"isByRequest\": \"N\",\n" +
                "                \"lastName\": \"Pingree\",\n" +
                "                \"party\": \"D\",\n" +
                "                \"state\": \"ME\",\n" +
                "                \"url\": \"https://api.congress.gov/v3/member/P000597?format=json\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"subjects\": {\n" +
                "            \"count\": 1,\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/subjects?format=json\"\n" +
                "        },\n" +
                "        \"textVersions\": {\n" +
                "            \"count\": 1,\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/text?format=json\"\n" +
                "        },\n" +
                "        \"title\": \"The Organic Dairy Data Collection Act\",\n" +
                "        \"titles\": {\n" +
                "            \"count\": 3,\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/titles?format=json\"\n" +
                "        },\n" +
                "        \"type\": \"HR\",\n" +
                "        \"updateDate\": \"2024-06-13T15:09:04Z\",\n" +
                "        \"updateDateIncludingText\": \"2024-06-13T15:09:04Z\"\n" +
                "    },\n" +
                "    \"request\": {\n" +
                "        \"billNumber\": \"6937\",\n" +
                "        \"billType\": \"hr\",\n" +
                "        \"congress\": \"118\",\n" +
                "        \"contentType\": \"application/json\",\n" +
                "        \"format\": \"json\"\n" +
                "    }\n" +
                "}";

        //Existing bill returned from bill repo
        Bill mockBill = new Bill();
        mockBill.setBillId("118-6937");
        mockBill.setCongress(118);
        mockBill.setBillNo(6937);
        mockBill.setBillTitle("The Organic Dairy Data Collection Act");
        mockBill.setOriginChamberCd("H");
        mockBill.setOriginChamber("House");
        mockBill.setBillType("HR");
        mockBill.setLatestActionDt(LocalDate.of(2024, 8, 2));
        mockBill.setLatestActionTxt("Latest action text test");

        when(billRepository.findByCongressAndBillNo(anyInt(), anyInt())).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertEquals("118-6937", processedBill.getBillId());
        assertEquals(6937, processedBill.getBillNo());
        assertEquals("The Organic Dairy Data Collection Act", processedBill.getBillTitle());
        assertEquals(LocalDate.of(2024, 1, 10), processedBill.getIntroducedDt());
        assertEquals(LocalDate.of(2024, 1, 10), processedBill.getLatestActionDt());
        assertEquals("Referred to the House Committee on Agriculture.", processedBill.getLatestActionTxt());
        assertEquals("Agriculture and Food", processedBill.getPolicyArea());
        assertEquals(118, processedBill.getCongress());
        assertEquals("HR", processedBill.getBillType());
        assertEquals("House", processedBill.getOriginChamber());
        assertEquals("H", processedBill.getOriginChamberCd());
    }

}
