package com.smsmode.booking.controller.impl;

import com.smsmode.booking.controller.BookingController;

import com.smsmode.booking.resource.booking.BookingGetResource;
import com.smsmode.booking.resource.booking.BookingItemPostResource;
import com.smsmode.booking.resource.booking.BookingPostResource;
import com.smsmode.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookingControllerImpl implements BookingController {

    private final BookingService bookingService;

    @Override
    public ResponseEntity<BookingGetResource> createBooking(BookingPostResource bookingPostResource) {
        return bookingService.create(bookingPostResource);
    }

    @Override
    public ResponseEntity<BookingGetResource> addItemToBooking(String bookingId, BookingItemPostResource bookingItemPostResource) {
        return bookingService.addItem(bookingId, bookingItemPostResource);
    }

    @Override
    public ResponseEntity<BookingGetResource> getBookingById(String bookingId) {
        return bookingService.retrieveById(bookingId);
    }
}