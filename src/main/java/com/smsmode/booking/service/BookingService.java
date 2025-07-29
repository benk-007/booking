package com.smsmode.booking.service;


import com.smsmode.booking.resource.booking.BookingGetResource;
import com.smsmode.booking.resource.booking.BookingItemPostResource;
import com.smsmode.booking.resource.booking.BookingPostResource;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    ResponseEntity<List<String>> retrieveBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict);

    ResponseEntity<BookingGetResource> create(BookingPostResource bookingPostResource);

    ResponseEntity<BookingGetResource> addItem(String bookingId, BookingItemPostResource bookingItemPostResource);

    ResponseEntity<BookingGetResource> retrieveById(String bookingId);
}