package com.rankandfile.backend.processor;

import com.rankandfile.backend.repository.BillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BillSubjectProcessor.
 *
 * This class tests the BillSubjectProcessor's methods to ensure they behave as expected
 * under various scenarios, including successful processing, handling of missing or
 * invalid data, and error conditions.
 */
@ExtendWith(MockitoExtension.class)
class BillSubjectProcessorTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillSubjectProcessor processor;

    /**
     * Test processing of legislative subjects with a valid JSON response.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsSuccessful() {
        String jsonResponse = "{\n" +
                "    \"subjects\": {\n" +
                "        \"legislativeSubjects\": [\n" +
                "            { \"name\": \"Criminal investigation, prosecution, interrogation\", \"updateDate\": \"2023-04-10T17:00:29Z\" },\n" +
                "            { \"name\": \"Criminal procedure and sentencing\", \"updateDate\": \"2023-04-10T17:00:29Z\" },\n" +
                "            { \"name\": \"Evidence and witnesses\", \"updateDate\": \"2023-04-10T17:00:29Z\" }\n" +
                "        ],\n" +
                "        \"policyArea\": { \"name\": \"Armed Forces and National Security\", \"updateDate\": \"2023-01-23T15:15:19Z\" }\n" +
                "    }\n" +
                "}";

        String billId = "hr3076-117";

        String expected = "Criminal investigation, prosecution, interrogation|Criminal procedure and sentencing|Evidence and witnesses";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertEquals(expected, result, "Processor should return a pipe-delimited string of subject names");
    }

    /**
     * Test processing when the legislativeSubjects array is empty.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsEmptySubjects() {
        String jsonResponse = "{\n" +
                "    \"subjects\": {\n" +
                "        \"legislativeSubjects\": []\n" +
                "    }\n" +
                "}";

        String billId = "hr3076-117";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertNull(result, "Processor should return null when legislativeSubjects array is empty");
    }

    /**
     * Test processing when the subjects field is missing from the JSON.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsMissingSubjects() {
        String jsonResponse = "{\n" +
                "    \"someOtherField\": {}\n" +
                "}";

        String billId = "hr3076-117";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertNull(result, "Processor should return null when subjects field is missing");
    }

    /**
     * Test processing when the legislativeSubjects field is missing within subjects.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsMissingLegislativeSubjects() {
        String jsonResponse = "{\n" +
                "    \"subjects\": {\n" +
                "        \"policyArea\": { \"name\": \"Armed Forces and National Security\", \"updateDate\": \"2023-01-23T15:15:19Z\" }\n" +
                "    }\n" +
                "}";

        String billId = "hr3076-117";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertNull(result, "Processor should return null when legislativeSubjects field is missing within subjects");
    }

    /**
     * Test processing with invalid JSON input.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsInvalidJson() {
        String jsonResponse = "Invalid JSON";

        String billId = "hr3076-117";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertNull(result, "Processor should return null when JSON is invalid and cannot be parsed");
    }

    /**
     * Test processing with some legislativeSubjects missing the name field or having empty names.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsSubjectsWithMissingName() {
        String jsonResponse = "{\n" +
                "    \"subjects\": {\n" +
                "        \"legislativeSubjects\": [\n" +
                "            { \"updateDate\": \"2023-04-10T17:00:29Z\" },\n" +
                "            { \"name\": \"\", \"updateDate\": \"2023-04-10T17:00:29Z\" },\n" +
                "            { \"name\": \"Valid Subject\", \"updateDate\": \"2023-04-10T17:00:29Z\" }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        String billId = "hr3076-117";

        String expected = "Valid Subject";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertEquals(expected, result, "Processor should include only valid subjects with non-empty names");
    }

    /**
     * Test processing when all legislativeSubjects have invalid or empty names.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsAllInvalidNames() {
        String jsonResponse = "{\n" +
                "    \"subjects\": {\n" +
                "        \"legislativeSubjects\": [\n" +
                "            { \"name\": \"\", \"updateDate\": \"2023-04-10T17:00:29Z\" },\n" +
                "            { \"name\": null, \"updateDate\": \"2023-04-10T17:00:29Z\" }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        String billId = "hr3076-117";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertNull(result, "Processor should return null when all legislativeSubjects have invalid or empty names");
    }

    /**
     * Test processing with legislativeSubjects having names with only whitespace and valid names with leading/trailing whitespace.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsWithWhitespaceNames() {
        String jsonResponse = "{\n" +
                "    \"subjects\": {\n" +
                "        \"legislativeSubjects\": [\n" +
                "            { \"name\": \"   \", \"updateDate\": \"2023-04-10T17:00:29Z\" },\n" +
                "            { \"name\": \"Valid Subject\", \"updateDate\": \"2023-04-10T17:00:29Z\" },\n" +
                "            { \"name\": \"  Another Valid Subject  \", \"updateDate\": \"2023-04-10T17:00:29Z\" }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        String billId = "hr3076-117";

        String expected = "Valid Subject|Another Valid Subject";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertEquals(expected, result, "Processor should trim valid subjects and exclude those with only whitespace");
    }

    /**
     * Test processing with additional unexpected fields in legislativeSubjects.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessLegislativeSubjectsAdditionalFieldsIgnored() {
        String jsonResponse = "{\n" +
                "    \"subjects\": {\n" +
                "        \"legislativeSubjects\": [\n" +
                "            { \"name\": \"Subject One\", \"updateDate\": \"2023-04-10T17:00:29Z\", \"extraField\": \"extraValue\" },\n" +
                "            { \"name\": \"Subject Two\", \"updateDate\": \"2023-04-10T17:00:29Z\" }\n" +
                "        ],\n" +
                "        \"policyArea\": { \"name\": \"Policy Area Name\", \"updateDate\": \"2023-01-23T15:15:19Z\" }\n" +
                "    },\n" +
                "    \"additionalData\": \"Some other data\"\n" +
                "}";

        String billId = "hr3076-117";

        String expected = "Subject One|Subject Two";

        String result = processor.processLegislativeSubjects(jsonResponse, billId);

        assertEquals(expected, result, "Processor should include valid subjects and ignore additional fields");
    }
}
