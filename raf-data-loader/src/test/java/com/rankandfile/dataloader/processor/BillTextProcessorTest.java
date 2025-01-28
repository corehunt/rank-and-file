package com.rankandfile.dataloader.processor;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.Text;
import com.rankandfile.dataloader.repository.TextRepository;
import com.rankandfile.dataloader.util.IdGenerator;
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
class BillTextProcessorTest {

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private TextRepository textRepository;

    @InjectMocks
    private BillTextProcessor processor;

    @Test
    void testProcessBillTextResponse() {
        // Mock IdGenerator
        when(idGenerator.generateTextId()).thenReturn("TEXT_ID_1", "TEXT_ID_2", "TEXT_ID_3");

        String json = "{\n" +
                "  \"pagination\": {\n" +
                "    \"count\": 8\n" +
                "  },\n" +
                "  \"request\": {\n" +
                "    \"billNumber\": \"3076\",\n" +
                "    \"billType\": \"hr\",\n" +
                "    \"billUrl\": \"https://api.congress.gov/v3/bill/117/hr/3076?format=json\",\n" +
                "    \"congress\": \"117\",\n" +
                "    \"contentType\": \"application/json\",\n" +
                "    \"format\": \"json\"\n" +
                "  },\n" +
                "  \"textVersions\": [\n" +
                "    {\n" +
                "      \"date\": null,\n" +
                "      \"formats\": [\n" +
                "        {\n" +
                "          \"type\": \"Formatted Text\",\n" +
                "          \"url\": \"https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076enr.htm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"PDF\",\n" +
                "          \"url\": \"https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076enr.pdf\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"Formatted XML\",\n" +
                "          \"url\": \"https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076enr.xml\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"type\": \"Enrolled Bill\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"date\": \"2022-02-15T05:00:00Z\",\n" +
                "      \"formats\": [\n" +
                "        {\n" +
                "          \"type\": \"Formatted Text\",\n" +
                "          \"url\": \"https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076pcs2.htm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"PDF\",\n" +
                "          \"url\": \"https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076pcs2.pdf\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"Formatted XML\",\n" +
                "          \"url\": \"https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076pcs2.xml\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"type\": \"Placed on Calendar Senate\"\n" +
                "    }\n" +
                "    // ... additional text versions\n" +
                "  ]\n" +
                "}";

        Bill mockBill = new Bill();
        mockBill.setBillId("hr3076-117");
        mockBill.setBillNo("3076");
        mockBill.setBillTitle("Postal Service Reform Act of 2022");
        mockBill.setCongress("117");
        mockBill.setBillType("hr");
        mockBill.setOriginChamberCd("H");
        mockBill.setOriginChamber("House");

        // Mock TextRepository to return no existing texts
        when(textRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());

        List<Text> textList = processor.processBillTextResponse(json, mockBill);

        assertEquals(2, textList.size());

        Text text1 = textList.get(0);
        assertEquals("TEXT_ID_1", text1.getTextId());
        assertNull(text1.getVersionDate());
        assertEquals("Enrolled Bill", text1.getVersionType());
        assertEquals("https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076enr.htm", text1.getFormattedTextUrl());
        assertEquals("https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076enr.pdf", text1.getPdfUrl());
        assertEquals("https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076enr.xml", text1.getXmlUrl());

        Text text2 = textList.get(1);
        assertEquals("TEXT_ID_2", text2.getTextId());
        assertEquals(LocalDate.of(2022, 2, 15), text2.getVersionDate());
        assertEquals("Placed on Calendar Senate", text2.getVersionType());
        assertEquals("https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076pcs2.htm", text2.getFormattedTextUrl());
        assertEquals("https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076pcs2.pdf", text2.getPdfUrl());
        assertEquals("https://www.congress.gov/117/bills/hr3076/BILLS-117hr3076pcs2.xml", text2.getXmlUrl());
    }

    @Test
    void testProcessBillTextResponseWithEmptyTextVersions() {
        String json = "{ \"textVersions\": [] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("hr3076-117");

        List<Text> textList = processor.processBillTextResponse(json, mockBill);
        assertEquals(0, textList.size());
    }

    @Test
    void testProcessBillTextResponseWithMissingFields() {
        String json = "{ \"textVersions\": [ { \"formats\": [ { \"type\": \"PDF\", \"url\": \"https://example.com/bill.pdf\" } ] } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("hr3076-117");

        when(textRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());
        when(idGenerator.generateTextId()).thenReturn("TEXT_ID_1");

        List<Text> textList = processor.processBillTextResponse(json, mockBill);
        assertEquals(1, textList.size());

        Text text = textList.get(0);
        assertEquals("TEXT_ID_1", text.getTextId());
        assertNull(text.getVersionDate());
        assertNull(text.getVersionType());
        assertNull(text.getFormattedTextUrl());
        assertEquals("https://example.com/bill.pdf", text.getPdfUrl());
        assertNull(text.getXmlUrl());
    }

    @Test
    void testProcessBillTextResponseWithNullFields() {
        String json = "{ \"textVersions\": [ { \"date\": null, \"type\": null, \"formats\": [ { \"type\": \"Formatted Text\", \"url\": null } ] } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("hr3076-117");

        when(textRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());
        when(idGenerator.generateTextId()).thenReturn("TEXT_ID_1");

        List<Text> textList = processor.processBillTextResponse(json, mockBill);
        assertEquals(1, textList.size());

        Text text = textList.get(0);
        assertEquals("TEXT_ID_1", text.getTextId());
        assertNull(text.getVersionDate());
        assertNull(text.getVersionType());
        assertNull(text.getFormattedTextUrl());
        assertNull(text.getPdfUrl());
        assertNull(text.getXmlUrl());
    }

    @Test
    void testProcessBillTextResponseWithInvalidDate() {
        String json = "{ \"textVersions\": [ { \"date\": \"invalid-date\", \"type\": \"Sample Type\", \"formats\": [] } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("hr3076-117");

        when(textRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.emptyList());
        when(idGenerator.generateTextId()).thenReturn("TEXT_ID_1");

        List<Text> textList = processor.processBillTextResponse(json, mockBill);
        assertEquals(1, textList.size());

        Text text = textList.get(0);
        assertEquals("TEXT_ID_1", text.getTextId());
        assertNull(text.getVersionDate());
        assertEquals("Sample Type", text.getVersionType());
    }

    @Test
    void testProcessBillTextResponseWithExistingTexts() {
        String json = "{ \"textVersions\": [ { \"date\": \"2022-02-15T05:00:00Z\", \"type\": \"Placed on Calendar Senate\", \"formats\": [] } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("3076-117");

        Text existingText = new Text();
        existingText.setTextId("EXISTING_TEXT_ID");
        existingText.setBill(mockBill);
        existingText.setVersionType("Placed on Calendar Senate");
        existingText.setVersionDate(LocalDate.of(2022, 2, 15));

        when(textRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.singletonList(existingText));

        List<Text> textList = processor.processBillTextResponse(json, mockBill);

        assertEquals(1, textList.size());
        Text text = textList.get(0);
        assertEquals("EXISTING_TEXT_ID", text.getTextId());

        // Verify that idGenerator.generateTextId() was not called
        verify(idGenerator, never()).generateTextId();
    }

    @Test
    void testProcessBillTextResponseWithAllExistingTexts() {
        String json = "{ \"textVersions\": [ { \"date\": \"2022-02-15T05:00:00Z\", \"type\": \"Placed on Calendar Senate\", \"formats\": [] }, { \"date\": null, \"type\": \"Enrolled Bill\", \"formats\": [] } ] }";

        Bill mockBill = new Bill();
        mockBill.setBillId("hr3076-117");

        Text existingText1 = new Text();
        existingText1.setTextId("EXISTING_TEXT_ID_1");
        existingText1.setBill(mockBill);
        existingText1.setVersionType("Placed on Calendar Senate");
        existingText1.setVersionDate(LocalDate.of(2022, 2, 15));

        Text existingText2 = new Text();
        existingText2.setTextId("EXISTING_TEXT_ID_2");
        existingText2.setBill(mockBill);
        existingText2.setVersionType("Enrolled Bill");
        existingText2.setVersionDate(null);

        when(textRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Arrays.asList(existingText1, existingText2));

        List<Text> textList = processor.processBillTextResponse(json, mockBill);

        assertEquals(2, textList.size());

        // Verify that idGenerator.generateTextId() was not called
        verify(idGenerator, never()).generateTextId();
    }

    @Test
    void testProcessBillTextResponseWithUnexpectedJsonStructure() {
        String json = "{ \"unexpectedField\": \"unexpectedValue\" }";

        Bill mockBill = new Bill();
        mockBill.setBillId("hr3076-117");

        List<Text> textList = processor.processBillTextResponse(json, mockBill);
        assertEquals(0, textList.size());
    }
}
