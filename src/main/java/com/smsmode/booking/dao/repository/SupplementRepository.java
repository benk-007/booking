package com.smsmode.booking.dao.repository;

import com.smsmode.booking.model.SupplementModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplementRepository extends JpaRepository<SupplementModel, String>, JpaSpecificationExecutor<SupplementModel> {
}