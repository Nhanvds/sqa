package com.doan.backend.services;

import com.doan.backend.dto.request.AddressRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.GHTKCostResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GHTKService {
    @Value("${ghtk.url}")
    private String apiUrl;

    @Value("${ghtk.token}")
    private String token;

    @Autowired
    private RestTemplate restTemplate;

    public ApiResponse<GHTKCostResponse> shippingCosts(AddressRequest addressRequest) {
        String url = String.format(
                "%s/services/shipment/fee?address=%s&province=%s&district=%s&ward=%s&pick_province=%s&pick_district=%s&pick_ward=%s&pick_address_id=%s&weight=%d&value=%d&deliver_option=%s",
                apiUrl,
                URLEncoder.encode(addressRequest.getAddressDetail(), StandardCharsets.UTF_8),
                URLEncoder.encode(addressRequest.getCity(), StandardCharsets.UTF_8),
                URLEncoder.encode(addressRequest.getDistrict(), StandardCharsets.UTF_8),
                URLEncoder.encode(addressRequest.getWard(), StandardCharsets.UTF_8),
                URLEncoder.encode("Hà Nội", StandardCharsets.UTF_8),
                URLEncoder.encode("Hà Đông", StandardCharsets.UTF_8),
                URLEncoder.encode("Mỗ Lao", StandardCharsets.UTF_8),
                "18359282",
                100,
                addressRequest.getValue().intValue(),
                "none"
        );

        System.out.println("url  " + url);

        System.out.println("token  " + token);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        headers.set("X-Client-Source", "S22810482");
        headers.set("Accept", "text/plain, application/json, application/* json, */*");
        headers.set("Host", apiUrl);
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        headers.set("Content-Length", "0");
        System.out.println("headers" + headers);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();

            System.out.println("responseEntity.getBody()" + responseEntity.getBody());

            GHTKCostResponse gHTKResponse = objectMapper.readValue(
                    responseEntity.getBody(),
                    new TypeReference<GHTKCostResponse>() {
                    }
            );
            System.out.println("gHTKResponse" + gHTKResponse);

            return ApiResponse.<GHTKCostResponse>builder()
                    .code(200)
                    .message("Get shipping cost successfully")
                    .result(gHTKResponse)
                    .build();
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error when calling GHTK API");
        }
    }
}
