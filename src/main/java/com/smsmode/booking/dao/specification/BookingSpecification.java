/**
 * <p>Copyright (C) Calade Technologies, Inc - All Rights Reserved Unauthorized copying of this
 * file, via any medium is strictly prohibited Proprietary and confidential
 */
package com.smsmode.booking.dao.specification;

import com.smsmode.booking.enumeration.BookingStatusEnum;
import com.smsmode.booking.enumeration.BookingTypeEnum;
import com.smsmode.booking.model.BookingModel;
import com.smsmode.booking.model.BookingModel_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

/**
 * TODO: add your documentation
 *
 * @author hamzahabchi (contact: hamza.habchi@messaging-technologies.com)
 * <p>Created 24 Jul 2025</p>
 */
public class BookingSpecification {

    public static Specification<BookingModel> withIdEqual(String bookingId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(BookingModel_.id), bookingId);
    }

    public static Specification<BookingModel> withStatus(BookingStatusEnum status) {
        return (root, query, criteriaBuilder) ->
                ObjectUtils.isEmpty(status) ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get(BookingModel_.status), status);
    }

    public static Specification<BookingModel> withType(BookingTypeEnum type) {
        return (root, query, criteriaBuilder) ->
                ObjectUtils.isEmpty(type) ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get(BookingModel_.type), type);
    }

    public static Specification<BookingModel> withParentBooking(BookingModel parentBooking) {
        return (root, query, criteriaBuilder) ->
                ObjectUtils.isEmpty(parentBooking) ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get(BookingModel_.parentBooking), parentBooking);
    }
}
