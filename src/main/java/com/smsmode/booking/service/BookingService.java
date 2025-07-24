package com.smsmode.booking.service;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    ResponseEntity<List<String>> retrieveBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict);

}
