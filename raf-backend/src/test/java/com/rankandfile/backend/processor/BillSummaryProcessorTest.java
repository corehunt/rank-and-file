package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BillSummaryProcessorTest {

    @Mock
    private BillRepository billRepository;

    private BillSummaryProcessor billSummaryProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        billSummaryProcessor = new BillSummaryProcessor(billRepository);
    }

    @Test
    void testProcessBillSummaryResponseSuccess() {
        // Sample JSON response with summaries
        String json = "{\n" +
                "  \"summaries\": [\n" +
                "    {\n" +
                "      \"actionDate\": \"2021-05-11\",\n" +
                "      \"actionDesc\": \"Introduced in House\",\n" +
                "      \"text\": \"<p>Summary Text 1</p>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"actionDate\": \"2022-04-06\",\n" +
                "      \"actionDesc\": \"Public Law\",\n" +
                "      \"text\": \"<p>Summary Text 2</p>\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bill bill = new Bill();
        bill.setBillNo("3076");
        bill.setCongress("117");

        // Invoke the processor method
        Bill updatedBill = billSummaryProcessor.processBillSummaryResponse(json, bill);

        // Verify that the bill's summary fields are updated with the most recent summary
        assertEquals(LocalDate.of(2022, 4, 6), updatedBill.getSummaryActionDt());
        assertEquals("Public Law", updatedBill.getSummaryActionDesc());
        assertEquals("Summary Text 2", updatedBill.getSummaryTxt());
    }

    @Test
    void testProcessBillSummaryResponseNoSummaries() {
        String json = "{ \"summaries\": [] }";

        Bill bill = new Bill();
        bill.setBillNo("3076");
        bill.setCongress("117");

        Bill updatedBill = billSummaryProcessor.processBillSummaryResponse(json, bill);

        // Verify that the bill's summary fields are not updated
        assertNull(updatedBill.getSummaryActionDt());
        assertNull(updatedBill.getSummaryActionDesc());
        assertNull(updatedBill.getSummaryTxt());
    }

    @Test
    void testProcessBillSummaryResponseInvalidDate() {
        String json = "{\n" +
                "  \"summaries\": [\n" +
                "    {\n" +
                "      \"actionDate\": \"invalid-date\",\n" +
                "      \"actionDesc\": \"Invalid Date\",\n" +
                "      \"text\": \"<p>Invalid Date Summary</p>\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bill bill = new Bill();
        bill.setBillNo("3076");
        bill.setCongress("117");

        Bill updatedBill = billSummaryProcessor.processBillSummaryResponse(json, bill);

        // Verify that summaryActionDt is null due to invalid date
        assertNull(updatedBill.getSummaryActionDt());
        assertEquals("Invalid Date", updatedBill.getSummaryActionDesc());
        assertEquals("Invalid Date Summary", updatedBill.getSummaryTxt());
    }
}
