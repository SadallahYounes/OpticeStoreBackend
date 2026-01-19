package com.opticstore.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesByDayResponse(
        LocalDate day,
        BigDecimal total
) {}