package com.rankandfile.dataloader.service.scheduled;

import com.rankandfile.dataloader.config.ApiConfig;
import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.processor.PersonProcessor;
import com.rankandfile.dataloader.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ScheduledCurrentMemberUpdateServiceTest {

    @Mock
    private WebClient mockWebClient;

    // Use raw types (not RequestHeadersUriSpec<?>)
    @Mock
    private WebClient.RequestHeadersUriSpec mockUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec mockHeadersSpec;

    @Mock
    private WebClient.ResponseSpec mockResponseSpec;

    @Mock
    private PersonProcessor mockPersonProcessor;

    @Mock
    private PersonRepository mockPersonRepository;

    @Mock
    private ApiConfig mockApiConfig;

    private ScheduledCurrentMemberUpdateService serviceUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        serviceUnderTest = new ScheduledCurrentMemberUpdateService(
                mockWebClient,
                mockPersonProcessor,
                mockPersonRepository,
                mockApiConfig
        );
    }

    @Test
    void updateMembers_successCase() {
        // Arrange
        List<String> personIds = List.of("A000001", "B000002");
        when(mockPersonRepository.findAllCurrentMemberIds()).thenReturn(personIds);

        // Mock the fluent WebClient chain
        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(any(Function.class))).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);

        // Return different responses for each ID
        when(mockResponseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("JSON_FOR_A000001"))
                .thenReturn(Mono.just("JSON_FOR_B000002"));

        // Processor returns valid Persons
        Person personA = new Person();
        personA.setPersonId("A000001");
        Person personB = new Person();
        personB.setPersonId("B000002");

        when(mockPersonProcessor.validatePerson("JSON_FOR_A000001")).thenReturn(personA);
        when(mockPersonProcessor.validatePerson("JSON_FOR_B000002")).thenReturn(personB);

        // Act
        serviceUnderTest.updateMembers();

        // Assert
        verify(mockPersonRepository).findAllCurrentMemberIds();
        verify(mockWebClient, times(2)).get();
        verify(mockPersonProcessor).validatePerson("JSON_FOR_A000001");
        verify(mockPersonProcessor).validatePerson("JSON_FOR_B000002");
        verify(mockPersonRepository).save(personA);
        verify(mockPersonRepository).save(personB);

        verifyNoMoreInteractions(mockWebClient, mockPersonProcessor, mockPersonRepository);
    }

    @Test
    void updateMembers_nullResponse() {
        // Arrange
        when(mockPersonRepository.findAllCurrentMemberIds()).thenReturn(List.of("C000003"));

        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(any(Function.class))).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);

        // Return Mono that yields null upon block()
        when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.justOrEmpty(null));

        // Act
        serviceUnderTest.updateMembers();

        // Assert
        verify(mockPersonProcessor, never()).validatePerson(anyString());
        verify(mockPersonRepository, never()).save(any(Person.class));
    }

    @Test
    void updateMembers_processorReturnsNull() {
        // Arrange
        when(mockPersonRepository.findAllCurrentMemberIds()).thenReturn(List.of("D000004"));

        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(any(Function.class))).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("VALID_JSON_BUT_NO_PERSON"));

        when(mockPersonProcessor.validatePerson("VALID_JSON_BUT_NO_PERSON")).thenReturn(null);

        // Act
        serviceUnderTest.updateMembers();

        // Assert
        verify(mockPersonRepository, never()).save(any(Person.class));
    }

    @Test
    void updateMembers_exceptionDuringFetch() {
        // Arrange
        when(mockPersonRepository.findAllCurrentMemberIds()).thenReturn(List.of("E000005"));

        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(any(Function.class))).thenReturn(mockHeadersSpec);

        // Throw an exception from retrieve()
        when(mockHeadersSpec.retrieve()).thenThrow(new RuntimeException("WebClient error"));

        // Act
        serviceUnderTest.updateMembers();

        // Assert
        verify(mockPersonProcessor, never()).validatePerson(anyString());
        verify(mockPersonRepository, never()).save(any(Person.class));
    }

    @Test
    void updateMembers_noIdsReturned() {
        // Arrange
        when(mockPersonRepository.findAllCurrentMemberIds()).thenReturn(Collections.emptyList());

        // Act
        serviceUnderTest.updateMembers();

        // Assert
        verify(mockWebClient, never()).get();
        verify(mockPersonProcessor, never()).validatePerson(anyString());
        verify(mockPersonRepository, never()).save(any(Person.class));
    }
}
