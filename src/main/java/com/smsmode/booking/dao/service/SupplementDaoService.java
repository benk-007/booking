package com.smsmode.booking.dao.service;

import com.smsmode.booking.model.SupplementModel;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SupplementDaoService {
    SupplementModel save(SupplementModel supplementModel);

    List<SupplementModel> findAllBy(Specification<SupplementModel> specification);

    void deleteBy(Specification<SupplementModel> specification);
}