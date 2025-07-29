package com.smsmode.booking.dao.service.impl;

import com.smsmode.booking.dao.repository.SupplementRepository;
import com.smsmode.booking.dao.service.SupplementDaoService;
import com.smsmode.booking.model.SupplementModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplementDaoServiceImpl implements SupplementDaoService {

    private final SupplementRepository supplementRepository;

    @Override
    public SupplementModel save(SupplementModel supplementModel) {
        return supplementRepository.save(supplementModel);
    }

    @Override
    public List<SupplementModel> findAllBy(Specification<SupplementModel> specification) {
        return supplementRepository.findAll(specification);
    }

    @Override
    @Transactional
    public void deleteBy(Specification<SupplementModel> specification) {
        List<SupplementModel> supplements = supplementRepository.findAll(specification);
        supplementRepository.deleteAll(supplements);
    }
}