package com.greatestbanking.orchestrator.api.config;

import com.greatestbanking.orchestrator.api.entity.OperationType;
import com.greatestbanking.orchestrator.api.repository.AppUserRepository;
import com.greatestbanking.orchestrator.api.repository.OperationTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private OperationTypeRepository operationTypeRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @SuppressWarnings("unchecked")
    void shouldSeedOperationTypes_whenRepositoryIsEmpty() {
        when(operationTypeRepository.count()).thenReturn(0L);

        dataInitializer.run(null);

        ArgumentCaptor<List<OperationType>> captor = ArgumentCaptor.forClass(List.class);
        verify(operationTypeRepository).saveAll(captor.capture());
        verify(appUserRepository, times(3)).save(any());

        List<OperationType> seeded = captor.getValue();
        assertThat(seeded).hasSize(4);
        assertThat(seeded).extracting(OperationType::getOperationTypeId)
                .containsExactly(1, 2, 3, 4);
        assertThat(seeded).extracting(OperationType::getDescription)
                .containsExactly("PURCHASE", "INSTALLMENT PURCHASE", "WITHDRAWAL", "PAYMENT");
    }

    @Test
    void shouldSkipSeeding_whenOperationTypesAlreadyExist() {
        when(operationTypeRepository.count()).thenReturn(4L);

        dataInitializer.run(null);

        verify(operationTypeRepository, never()).saveAll(anyList());
        verify(appUserRepository, times(3)).save(any());
    }

    @Test
    void shouldSkipMockUsersThatAlreadyExist() {
        when(operationTypeRepository.count()).thenReturn(4L);
        when(appUserRepository.existsByUsername(anyString())).thenReturn(true);

        dataInitializer.run(null);

        verify(operationTypeRepository, never()).saveAll(anyList());
        verify(appUserRepository, times(3)).existsByUsername(anyString());
        verify(appUserRepository, never()).save(any());
    }
}
