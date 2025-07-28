package com.smsmode.booking.dao.service;

import java.time.LocalDate;
import java.util.List;

public interface BookingDaoService {
    List<String> findBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict);
}