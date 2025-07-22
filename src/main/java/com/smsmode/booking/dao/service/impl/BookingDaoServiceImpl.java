package com.smsmode.booking.dao.service.impl;


import com.smsmode.booking.dao.repository.BookingRepository;
import com.smsmode.booking.dao.service.BookingDaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingDaoServiceImpl implements BookingDaoService {
    private final BookingRepository bookingRepository;

    @Override
    public List<String> findAllByDateOverlap(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching reserved unitIds overlapping between {} and {}", startDate, endDate);
        return bookingRepository.findStrictlyOverlappingConfirmedSingleUnitIds(startDate, endDate);
    }
}
