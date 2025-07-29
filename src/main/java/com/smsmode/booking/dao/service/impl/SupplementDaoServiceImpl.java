package com.smsmode.booking.dao.service.impl;

import com.smsmode.booking.dao.repository.SupplementRepository;
import com.smsmode.booking.dao.service.SupplementDaoService;
import com.smsmode.booking.model.SupplementModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplementDaoServiceImpl implements SupplementDaoService {
    private final SupplementRepository supplementRepository;

    @Override
    public SupplementModel save(SupplementModel supplementModel) {
        return supplementRepository.save(supplementModel);
    }

    @Override
    public List<SupplementModel> findByBookingId(String bookingId) {
        return supplementRepository.findByBookingId(bookingId);
    }
}
