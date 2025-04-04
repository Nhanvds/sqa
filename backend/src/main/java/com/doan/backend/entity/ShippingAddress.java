package com.doan.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "shipping_addresses")
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "recipient_name", nullable = false)
    String recipientName;

    @Column(name = "phone_number", nullable = false)
    String phoneNumber;

    @Column(name = "address_detail", nullable = false)
    String addressDetail;

    @Column(name = "country", nullable = false)
    String country;

    @Column(name = "city", nullable = false)
    String city;

    @Column(name = "district", nullable = false)
    String district;

    @Column(name = "ward", nullable = false)
    String ward;

    @Column(name = "is_default", nullable = false)
    Boolean isDefault;
}
