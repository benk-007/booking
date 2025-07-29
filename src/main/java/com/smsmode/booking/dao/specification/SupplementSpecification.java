package com.smsmode.booking.dao.specification;

import com.smsmode.booking.model.SupplementModel;
import com.smsmode.booking.model.SupplementModel_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

/**
 * Specification class for SupplementModel queries
 */
public class SupplementSpecification {

    public static Specification<SupplementModel> withBookingId(String bookingId) {
        return (root, query, criteriaBuilder) ->
                ObjectUtils.isEmpty(bookingId) ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get(SupplementModel_.bookingId), bookingId);
    }
}