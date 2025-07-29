package com.smsmode.booking.embeddable;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Embeddable class representing party information in bookings.
 */
@Data
@Embeddable
public class PartyEmbeddable {
    @Column(name = "PARTY_ID")
    private String id;
    @Column(name = "PARTY_NAME")
    private String name;


    private ContactEmbeddable contact;
}