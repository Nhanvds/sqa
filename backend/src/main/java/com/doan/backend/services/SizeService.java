package com.doan.backend.services;

import com.doan.backend.dto.request.SizeRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.entity.Size;
import com.doan.backend.mapper.SizeMapper;
import com.doan.backend.repositories.SizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class SizeService {
    SizeRepository sizeRepository;
    SizeMapper sizeMapper;

    public ApiResponse<Size> createSize(SizeRequest sizeRequest) {
        if (sizeRepository.existsByName(sizeRequest.getName())) {
            throw new RuntimeException("Size is already exist");
        }
        Size size = sizeMapper.toSize(sizeRequest);
        return ApiResponse.<Size>builder()
                .code(200)
                .result(sizeRepository.save(size))
                .build();
    }

    public ApiResponse<List<Size>> getAllSize() {
        return ApiResponse.<List<Size>>builder()
                .code(200)
                .result(sizeRepository.findAll())
                .build();
    }

    public ApiResponse<Size> getSizeById(String id) {
        return ApiResponse.<Size>builder()
                .code(200)
                .result(sizeRepository.findById(id).orElseThrow(() -> new RuntimeException("Size not found")))
                .build();
    }

    public ApiResponse<Size> updateSize(String id, SizeRequest sizeRequest) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Size not found"));
        size.setName(sizeRequest.getName());
        return ApiResponse.<Size>builder()
                .code(200)
                .result(sizeRepository.save(size))
                .build();
    }

    public ApiResponse<Void> deleteSize(String id) {
        sizeRepository.deleteById(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Delete size successfully")
                .build();
    }
}
