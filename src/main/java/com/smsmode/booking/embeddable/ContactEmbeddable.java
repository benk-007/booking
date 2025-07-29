package com.smsmode.booking.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class ContactEmbeddable {
    private String mobile;
    private String email;
}
