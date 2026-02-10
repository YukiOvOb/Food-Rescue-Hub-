package com.frh.backend.service;

import com.frh.backend.dto.Co2CategoryBreakdownDto;
import com.frh.backend.dto.Co2SummaryDto;
import com.frh.backend.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Co2AnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private Co2AnalyticsService co2AnalyticsService;

    @Test
    void getCo2Summary_normalizesDaysAndSumsOnlyNonNullValues() {
        Co2CategoryBreakdownDto meats = new Co2CategoryBreakdownDto(
            1L,
            "Meats",
            new BigDecimal("1.500"),
            null
        );
        Co2CategoryBreakdownDto veg = new Co2CategoryBreakdownDto(
            2L,
            "Vegetables",
            null,
            new BigDecimal("3.250")
        );

        when(orderRepository.findCo2BreakdownBySupplierAndStatusSince(eq(88L), eq("COMPLETED"), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
            .thenReturn(List.of(meats, veg));

        Co2SummaryDto result = co2AnalyticsService.getCo2Summary(88L, 0);

        assertEquals(1, result.getDays());
        assertEquals(new BigDecimal("1.500"), result.getTotalWeightKg());
        assertEquals(new BigDecimal("3.250"), result.getTotalCo2Kg());
        assertEquals(2, result.getCategories().size());
    }

    @Test
    void getCo2Summary_usesProvidedPositiveDaysForQueryWindow() {
        when(orderRepository.findCo2BreakdownBySupplierAndStatusSince(eq(9L), eq("COMPLETED"), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
            .thenReturn(List.of());

        Co2SummaryDto result = co2AnalyticsService.getCo2Summary(9L, 7);

        assertEquals(7, result.getDays());
        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(orderRepository).findCo2BreakdownBySupplierAndStatusSince(eq(9L), eq("COMPLETED"), sinceCaptor.capture());
        assertTrue(sinceCaptor.getValue().isBefore(result.getTo()));
        assertNotNull(result.getFrom());
        assertNotNull(result.getTo());
    }

    @Test
    void generateCo2ReportPdf_handlesNullValuesAndProducesBytes() {
        Co2SummaryDto summary = new Co2SummaryDto();
        summary.setFrom(LocalDateTime.now().minusDays(1));
        summary.setTo(LocalDateTime.now());
        summary.setTotalCo2Kg(null);
        summary.setTotalWeightKg(null);
        summary.setCategories(List.of(
            new Co2CategoryBreakdownDto(1L, "Meats", null, null)
        ));

        byte[] pdf = co2AnalyticsService.generateCo2ReportPdf(summary, 123L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }
}
