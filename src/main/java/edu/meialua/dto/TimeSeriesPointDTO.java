package edu.meialua.dto;

import java.time.LocalDate;

public record TimeSeriesPointDTO(
        LocalDate date,
        long count
) {
}
