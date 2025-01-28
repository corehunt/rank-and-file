package com.rankandfile.dataloader.processor;

import com.rankandfile.dataloader.entity.Committee;
import com.rankandfile.dataloader.repository.CommitteeRepository;
import com.rankandfile.dataloader.util.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommitteeProcessorTest {

    @Mock
    private CommitteeRepository committeeRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private CommitteeProcessor committeeProcessor;

    @Test
    void testProcessValidCommittee() {
        String json = "{\n" +
                "  \"committees\": [\n" +
                "    {\n" +
                "      \"systemCode\": \"SC001\",\n" +
                "      \"name\": \"Committee A\",\n" +
                "      \"chamber\": \"House\",\n" +
                "      \"committeeTypeCode\": \"Type1\",\n" +
                "      \"url\": \"http://example.com/committeeA\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(committeeRepository.findBySysCode("SC001")).thenReturn(null);
        when(idGenerator.generateCommitteeId("SC001")).thenReturn("CM001");

        // Execute the method
        List<Committee> result = committeeProcessor.process(json);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        Committee committee = result.get(0);
        assertEquals("CM001", committee.getCommitteeId());
        assertEquals("SC001", committee.getSysCode());
        assertEquals("Committee A", committee.getCommName());
        assertEquals("House", committee.getChamber());
        assertEquals("Type1", committee.getCommTypeCd());
        assertEquals("http://example.com/committeeA", committee.getUrlSrc());

        // Verify interactions
        verify(committeeRepository).findBySysCode("SC001");
        verify(idGenerator).generateCommitteeId("SC001");
        verify(committeeRepository).save(committee);
    }

    @Test
    void testProcessCommitteeWithSubcommittees() {
        String json = "{\n" +
                "  \"committees\": [\n" +
                "    {\n" +
                "      \"systemCode\": \"SC001\",\n" +
                "      \"name\": \"Committee A\",\n" +
                "      \"subcommittees\": [\n" +
                "        {\n" +
                "          \"systemCode\": \"SC002\",\n" +
                "          \"name\": \"Subcommittee A1\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"systemCode\": \"SC003\",\n" +
                "          \"name\": \"Subcommittee A2\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(committeeRepository.findBySysCode("SC001")).thenReturn(null);
        when(committeeRepository.findBySysCode("SC002")).thenReturn(null);
        when(committeeRepository.findBySysCode("SC003")).thenReturn(null);

        when(idGenerator.generateCommitteeId("SC001")).thenReturn("CM001");
        when(idGenerator.generateCommitteeId("SC002")).thenReturn("CM002");
        when(idGenerator.generateCommitteeId("SC003")).thenReturn("CM003");

        // Execute the method
        List<Committee> result = committeeProcessor.process(json);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        Committee parentCommittee = result.get(0);
        assertEquals("CM001", parentCommittee.getCommitteeId());
        assertEquals("Committee A", parentCommittee.getCommName());

        List<Committee> subCommittees = parentCommittee.getSubCommittees();
        assertNotNull(subCommittees);
        assertEquals(2, subCommittees.size());

        Committee subCommittee1 = subCommittees.get(0);
        assertEquals("CM002", subCommittee1.getCommitteeId());
        assertEquals("Subcommittee A1", subCommittee1.getCommName());
        assertEquals(parentCommittee, subCommittee1.getParent());

        Committee subCommittee2 = subCommittees.get(1);
        assertEquals("CM003", subCommittee2.getCommitteeId());
        assertEquals("Subcommittee A2", subCommittee2.getCommName());
        assertEquals(parentCommittee, subCommittee2.getParent());

        // Verify interactions
        verify(committeeRepository).findBySysCode("SC001");
        verify(committeeRepository).findBySysCode("SC002");
        verify(committeeRepository).findBySysCode("SC003");
        verify(idGenerator).generateCommitteeId("SC001");
        verify(idGenerator).generateCommitteeId("SC002");
        verify(idGenerator).generateCommitteeId("SC003");
        verify(committeeRepository, times(4)).save(any(Committee.class));
    }

    @Test
    void testProcessCommitteeWithParent() {
        String json = "{\n" +
                "  \"committees\": [\n" +
                "    {\n" +
                "      \"systemCode\": \"SC002\",\n" +
                "      \"name\": \"Subcommittee B1\",\n" +
                "      \"parent\": {\n" +
                "        \"systemCode\": \"SC001\",\n" +
                "        \"name\": \"Committee B\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(committeeRepository.findBySysCode("SC001")).thenReturn(null);
        when(committeeRepository.findBySysCode("SC002")).thenReturn(null);
        when(idGenerator.generateCommitteeId("SC001")).thenReturn("CM001");
        when(idGenerator.generateCommitteeId("SC002")).thenReturn("CM002");

        // Execute the method
        List<Committee> result = committeeProcessor.process(json);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        Committee subCommittee = result.get(0);
        assertEquals("CM002", subCommittee.getCommitteeId());
        assertEquals("Subcommittee B1", subCommittee.getCommName());

        Committee parentCommittee = subCommittee.getParent();
        assertNotNull(parentCommittee);
        assertEquals("CM001", parentCommittee.getCommitteeId());
        assertEquals("Committee B", parentCommittee.getCommName());

        // Verify interactions
        verify(committeeRepository).findBySysCode("SC002");
        verify(committeeRepository).findBySysCode("SC001");
        verify(idGenerator).generateCommitteeId("SC002");
        verify(idGenerator).generateCommitteeId("SC001");
        verify(committeeRepository, times(2)).save(any(Committee.class));
    }

    @Test
    void testProcessExistingCommittee() {
        String json = "{\n" +
                "  \"committees\": [\n" +
                "    {\n" +
                "      \"systemCode\": \"SC001\",\n" +
                "      \"name\": \"Updated Committee A\",\n" +
                "      \"chamber\": \"Senate\",\n" +
                "      \"committeeTypeCode\": \"Type2\",\n" +
                "      \"url\": \"http://example.com/updatedCommitteeA\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Committee existingCommittee = new Committee();
        existingCommittee.setCommitteeId("CM001");
        existingCommittee.setSysCode("SC001");
        existingCommittee.setCommName("Committee A");
        existingCommittee.setChamber("House");
        existingCommittee.setCommTypeCd("Type1");
        existingCommittee.setUrlSrc("http://example.com/committeeA");

        when(committeeRepository.findBySysCode("SC001")).thenReturn(existingCommittee);

        // Execute the method
        List<Committee> result = committeeProcessor.process(json);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());

        Committee updatedCommittee = result.get(0);
        assertEquals("CM001", updatedCommittee.getCommitteeId());
        assertEquals("Updated Committee A", updatedCommittee.getCommName());
        assertEquals("Senate", updatedCommittee.getChamber());
        assertEquals("Type2", updatedCommittee.getCommTypeCd());
        assertEquals("http://example.com/updatedCommitteeA", updatedCommittee.getUrlSrc());

        // Verify interactions
        verify(committeeRepository).findBySysCode("SC001");
        verify(committeeRepository).save(updatedCommittee);
        verifyNoInteractions(idGenerator);
    }

    @Test
    void testProcessNullJson() {
        // Execute the method
        List<Committee> result = committeeProcessor.process(null);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify that no interactions occur
        verifyNoInteractions(committeeRepository, idGenerator);
    }

    @Test
    void testProcessEmptyJson() {
        // Execute the method
        List<Committee> result = committeeProcessor.process("");

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify that no interactions occur
        verifyNoInteractions(committeeRepository, idGenerator);
    }

    @Test
    void testProcessInvalidJson() {
        String json = "{ invalid json }";

        // Execute the method
        List<Committee> result = committeeProcessor.process(json);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify that no interactions occur
        verifyNoInteractions(committeeRepository, idGenerator);
    }

    @Test
    void testProcessJsonWithMissingFields() {
        String json = "{\n" +
                "  \"committees\": [\n" +
                "    {\n" +
                "      \"name\": \"Committee Without SystemCode\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Execute the method
        List<Committee> result = committeeProcessor.process(json);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify interactions
        verifyNoInteractions(committeeRepository, idGenerator);
    }
}
