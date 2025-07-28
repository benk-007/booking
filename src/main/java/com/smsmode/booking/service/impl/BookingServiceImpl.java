package com.smsmode.booking.service.impl;

import com.smsmode.booking.dao.service.BookingDaoService;
import com.smsmode.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingDaoService bookingDaoService;

    @Override
    public ResponseEntity<List<String>> retrieveBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict) {
        return ResponseEntity.ok(bookingDaoService.findBookedUnitIds(checkinDate, checkoutDate, strict));
    }
}
