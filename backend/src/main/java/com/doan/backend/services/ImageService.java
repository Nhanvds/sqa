package com.doan.backend.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {
    @Autowired
    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public ApiResponse<String> uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty or not provided");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds the 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new FileUploadException("Invalid file type. Only JPEG and PNG are allowed.");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("Image uploaded successfully")
                    .result(uploadResult.get("url").toString())
                    .build();
        } catch (IOException e) {
            throw new FileUploadException("Error occurred while uploading image: " + e.getMessage());
        }
    }

    public ApiResponse<String> deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("Image deleted successfully")
                    .result(publicId)
                    .build();
        } catch (IOException e) {
            throw new FileUploadException("Error occurred while deleting image: " + e.getMessage());
        }
    }
}
