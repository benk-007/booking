package com.smsmode.booking.dao.service;

import com.smsmode.booking.model.SupplementModel;

import java.util.List;

public interface SupplementDaoService {
    SupplementModel save(SupplementModel supplementModel);
    List<SupplementModel> findByBookingId(String bookingId);
}