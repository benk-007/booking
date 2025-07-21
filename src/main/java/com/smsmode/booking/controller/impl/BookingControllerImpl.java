package com.smsmode.booking.controller.impl;

import com.smsmode.booking.controller.BookingController;
import com.smsmode.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingControllerImpl implements BookingController {

    private final BookingService bookingService;

    @Override
    public ResponseEntity<List<String>> getReservedUnits(LocalDate startDate, LocalDate endDate) {
        return bookingService.getReservedUnits(startDate, endDate);
    }
}
