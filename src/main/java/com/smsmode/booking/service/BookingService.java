package com.smsmode.booking.service;


import com.smsmode.booking.resource.booking.get.BookingGetResource;
import com.smsmode.booking.resource.booking.patch.BookingPatchResource;
import com.smsmode.booking.resource.booking.post.BookingItemPostResource;
import com.smsmode.booking.resource.booking.post.BookingPostResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    ResponseEntity<List<String>> retrieveBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict);

    ResponseEntity<BookingGetResource> create(BookingPostResource bookingPostResource);

    ResponseEntity<BookingGetResource> addItem(String bookingId, BookingItemPostResource bookingItemPostResource);

    ResponseEntity<BookingGetResource> retrieveById(String bookingId);

    ResponseEntity<Page<BookingGetResource>> retrieveDraftGroupBookings(Pageable pageable);

    ResponseEntity<Void> deleteById(String bookingId);

    ResponseEntity<BookingGetResource> updateBooking(String bookingId, BookingPatchResource bookingPatchResource);
}