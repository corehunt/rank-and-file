package com.rankandfile.dataloader.service;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.SponsoredLegislation;
import com.rankandfile.dataloader.processor.CoSponsoredLegislationProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.repository.SponsoredLegislationRepository;
import com.rankandfile.dataloader.service.external.bill.BillCoSponsorService;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillCoSponsorServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private WebClient webClient;

    @Mock
    private BillRepository billRepository;

    @Mock
    private SponsoredLegislationRepository sponsoredLegislationRepository;

    @Mock
    private CoSponsoredLegislationProcessor coSponsoredLegislationProcessor;

    private BillCoSponsorService billCoSponsorService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // Build the WebClient using the baseUrl of the mock server
        WebClient.Builder webClientBuilder = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(addApiKeyQueryParamFilter());

        this.webClient = webClientBuilder.build();

        billCoSponsorService = new BillCoSponsorService(
                this.webClient,
                billRepository,
                sponsoredLegislationRepository,
                coSponsoredLegislationProcessor
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    /**
     * Adds an API key as a query param to mimic the reference code's approach.
     */
    private ExchangeFilterFunction addApiKeyQueryParamFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            URI updatedUri = UriComponentsBuilder.fromUri(clientRequest.url())
                    .queryParam("api_key", "test_api_key")
                    .build(true)
                    .toUri();

            ClientRequest updatedRequest = ClientRequest.from(clientRequest)
                    .url(updatedUri)
                    .build();

            return Mono.just(updatedRequest);
        });
    }

    @Test
    void testGetCoSponsorsByBillNumberBillNotFound() {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";

        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(null);

        billCoSponsorService.getCoSponsorsByBillNumber(congressNo, billType, billNo);

        verify(billRepository)
                .findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verifyNoInteractions(coSponsoredLegislationProcessor);
        verifyNoInteractions(sponsoredLegislationRepository);
        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    void testGetCoSponsorsByBillNumberEmptyResponse() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";

        Bill bill = new Bill();
        bill.setBillId("BILL-1234");

        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(bill);

        // Mock a server response with an empty body
        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        billCoSponsorService.getCoSponsorsByBillNumber(congressNo, billType, billNo);

        verify(billRepository)
                .findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verifyNoInteractions(coSponsoredLegislationProcessor);
        verify(sponsoredLegislationRepository, never()).saveAll(any());
        verify(billRepository, never()).save(any());

        // Verify request to MockWebServer
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
        assertTrue(recordedRequest.getPath().contains("/cosponsors"));
    }

    @Test
    void testGetCoSponsorsByBillNumberExceptionDuringFetch() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";

        Bill bill = new Bill();
        bill.setBillId("BILL-1234");

        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(bill);

        // Enqueue a response with a 500 status to simulate an error
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        Exception exception = assertThrows(Exception.class,
                () -> billCoSponsorService.getCoSponsorsByBillNumber(congressNo, billType, billNo));

        assertNotNull(exception);
        verify(billRepository)
                .findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verifyNoInteractions(coSponsoredLegislationProcessor);
        verify(sponsoredLegislationRepository, never()).saveAll(any());
        verify(billRepository, never()).save(any());

        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testGetCoSponsorsByBillNumberExceptionDuringProcessing() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";

        Bill bill = new Bill();
        bill.setBillId("BILL-1234");

        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(bill);

        // Mock a valid JSON response
        String responseBody = "{ \"cosponsors\": [ {\"bioguideId\": \"P1234\"} ] }";
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        // Mock processor to throw an exception
        doThrow(new RuntimeException("Processing error"))
                .when(coSponsoredLegislationProcessor).process(responseBody, bill);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> billCoSponsorService.getCoSponsorsByBillNumber(congressNo, billType, billNo));

        assertNotNull(exception);
        assertEquals("Processing error", exception.getMessage());
        verify(billRepository)
                .findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verify(coSponsoredLegislationProcessor).process(responseBody, bill);
        verifyNoInteractions(sponsoredLegislationRepository);
        verify(billRepository, never()).save(any());

        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testGetCoSponsorsByBillNumberSuccess() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";

        Bill bill = new Bill();
        bill.setBillId("BILL-1234");
        bill.setBillNo(billNo);
        bill.setBillType(billType);
        bill.setCongress(congressNo);

        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(bill);

        String responseBody = "{ \"cosponsors\": [ {\"bioguideId\": \"P1234\"}, {\"bioguideId\": \"P5678\"} ] }";

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to return a list of SponsoredLegislation
        SponsoredLegislation sl1 = new SponsoredLegislation();
        sl1.setSponLegId("SL-1");

        SponsoredLegislation sl2 = new SponsoredLegislation();
        sl2.setSponLegId("SL-2");

        List<SponsoredLegislation> slList = List.of(sl1, sl2);
        when(coSponsoredLegislationProcessor.process(responseBody, bill)).thenReturn(slList);

        billCoSponsorService.getCoSponsorsByBillNumber(congressNo, billType, billNo);

        verify(billRepository)
                .findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verify(coSponsoredLegislationProcessor).process(responseBody, bill);
        verify(billRepository).save(bill);
        verify(sponsoredLegislationRepository).saveAll(slList);

        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
        assertTrue(recordedRequest.getPath().contains("/cosponsors"));
    }
}
