package com.doan.backend.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class OrderTransport {
    String pick_address_id;
    String pick_address;
    String pick_province;
    String pick_district;
    String pick_ward;
    String pick_street;

    String address;
    String province;
    String district;
    String ward;
    String street;

    Integer weight;
    Integer value;
    String transport;
    String deliver_option;
    List<Integer> tags;
}
