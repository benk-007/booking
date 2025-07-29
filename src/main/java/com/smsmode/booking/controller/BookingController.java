/**
 * <p>Copyright (C) Calade Technologies, Inc - All Rights Reserved Unauthorized copying of this
 * file, via any medium is strictly prohibited Proprietary and confidential
 */
package com.smsmode.booking.controller;

import com.smsmode.booking.resource.booking.BookingGetResource;
import com.smsmode.booking.resource.booking.BookingItemPostResource;
import com.smsmode.booking.resource.booking.BookingPostResource;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TODO: add your documentation
 *
 * @author hamzahabchi (contact: hamza.habchi@messaging-technologies.com)
 * <p>Created 07 Jul 2025</p>
 */

@RequestMapping("/bookings")
public interface BookingController {
    @PostMapping
    ResponseEntity<BookingGetResource> createBooking(@RequestBody @Valid BookingPostResource bookingPostResource);

    @PostMapping("/{bookingId}/items")
    ResponseEntity<BookingGetResource> addItemToBooking(
            @PathVariable("bookingId") String bookingId,
            @RequestBody @Valid BookingItemPostResource bookingItemPostResource);

    @GetMapping("/{bookingId}")
    ResponseEntity<BookingGetResource> getBookingById(@PathVariable("bookingId") String bookingId);
}

