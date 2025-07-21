package com.smsmode.booking.service;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    ResponseEntity<List<String>> getReservedUnits(LocalDate startDate, LocalDate endDate);
}
