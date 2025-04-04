package com.doan.backend.controllers;

import com.doan.backend.dto.request.SizeRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.entity.Size;
import com.doan.backend.services.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/size")
public class SizeController {

    @Autowired
    private SizeService sizeService;

    @GetMapping("/get-all")
    public ApiResponse<?> getAllSize() {
        return sizeService.getAllSize();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getSizeById(@PathVariable String id) {
        return sizeService.getSizeById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteSize(@PathVariable String id) {
        return sizeService.deleteSize(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> updateSize(@PathVariable  String id, @RequestBody @Validated SizeRequest sizeRequest) {
        return sizeService.updateSize(id, sizeRequest);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Size> createSize(@RequestBody @Validated SizeRequest sizeRequest) {
        return sizeService.createSize(sizeRequest);
    }
}
