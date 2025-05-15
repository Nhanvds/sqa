package com.doan.backend.services;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.exception.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;


@ExtendWith(MockitoExtension.class)
public class TestImageService {

    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;
    @InjectMocks private ImageService imageService;

    // ===============================================================
    // Cấu hình chung: lenient stub để mỗi test riêng lẻ không phải gọi `cloudinary.uploader()` nữa.
    // ===============================================================
    @BeforeEach
    void setupUploader() {
        // đánh dấu lenient to tránh UnnecessaryStubbingException
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    // ===============================================================
    // TC-IS-001: Upload thành công file JPEG dưới 5MB
    // Mục tiêu: Đảm bảo uploadImage trả về URL khi file hợp lệ
    // Input: MultipartFile JPEG kích thước nhỏ hơn 5MB
    // Expected: ApiResponse.code=200, message="Image uploaded successfully", result = URL từ uploadResult
    // ===============================================================
    @Test
    public void testUploadImage_Success_Jpeg() throws Exception {
        byte[] content = new byte[1024]; // 1KB
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", content);

        Map<String,Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "http://cdn/test.jpg");
        when(uploader.upload(eq(content), anyMap())).thenReturn(uploadResult);

        ApiResponse<String> resp = imageService.uploadImage(file);

        assertEquals(200, resp.getCode());
        assertEquals("Image uploaded successfully", resp.getMessage());
        assertEquals("http://cdn/test.jpg", resp.getResult());
    }

    // ===============================================================
    // TC-IS-002: Upload thất bại khi file null hoặc rỗng
    // Mục tiêu: Đảm bảo FileUploadException khi file null hoặc empty
    // Input: file = null và file có getSize()=0
    // Expected: FileUploadException("File is empty or not provided")
    // ===============================================================
    @Test
    public void testUploadImage_Failure_EmptyFile_Null() {
        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> imageService.uploadImage(null));
        assertEquals("File is empty or not provided", ex.getMessage());
    }

    @Test
    public void testUploadImage_Failure_EmptyFile_ZeroSize() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]);
        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> imageService.uploadImage(file));
        assertEquals("File is empty or not provided", ex.getMessage());
    }

    // ===============================================================
    // TC-IS-003: Upload thất bại khi file quá 5MB
    // Mục tiêu: Đảm bảo FileUploadException khi file.getSize() > MAX_FILE_SIZE
    // Input: MultipartFile JPEG kích thước 6MB
    // Expected: FileUploadException("File size exceeds the 5MB limit")
    // ===============================================================
    @Test
    public void testUploadImage_Failure_TooLarge() {
        byte[] large = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", large);
        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> imageService.uploadImage(file));
        assertEquals("File size exceeds the 5MB limit", ex.getMessage());
    }

    // ===============================================================
    // TC-IS-004: Upload thất bại khi kiểu file không hợp lệ
    // Mục tiêu: Đảm bảo FileUploadException khi contentType không phải JPEG/PNG/WEBP
    // Input: MultipartFile text/plain
    // Expected: FileUploadException("Invalid file type. Only JPEG and PNG are allowed.")
    // ===============================================================
    @Test
    public void testUploadImage_Failure_InvalidType() {
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", content);
        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> imageService.uploadImage(file));
        assertEquals("Invalid file type. Only JPEG and PNG are allowed.", ex.getMessage());
    }

    // ===============================================================
    // TC-IS-005: Upload thất bại khi Cloudinary ném IOException
    // Mục tiêu: Đảm bảo FileUploadException khi upload() ném IOException
    // Input: MultipartFile PNG hợp lệ, uploader.upload throws IOException
    // Expected: FileUploadException("Error occurred while uploading image: <msg>")
    // ===============================================================
    @Test
    public void testUploadImage_Failure_IOException() throws Exception {
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", content);
        when(uploader.upload(eq(content), anyMap()))
                .thenThrow(new IOException("net error"));

        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> imageService.uploadImage(file));
        assertTrue(ex.getMessage().contains("Error occurred while uploading image: net error"));
    }

    // ===============================================================
    // TC-IS-006: Xóa ảnh thành công
    // Mục tiêu: Đảm bảo deleteImage trả về publicId khi Cloudinary.destroy thành công
    // Input: publicId = "abc123"
    // Expected: ApiResponse.code=200, message="Image deleted successfully", result="abc123"
    // ===============================================================
    @Test
    public void testDeleteImage_Success() throws Exception {
        // destroy returns a Map, not void
        when(uploader.destroy("abc123", ObjectUtils.emptyMap()))
                .thenReturn(Collections.emptyMap());

        ApiResponse<String> resp = imageService.deleteImage("abc123");

        assertEquals(200, resp.getCode());
        assertEquals("Image deleted successfully", resp.getMessage());
        assertEquals("abc123", resp.getResult());
    }

    // ===============================================================
    // TC-IS-007: Xóa ảnh thất bại khi Cloudinary.destroy ném IOException
    // Mục tiêu: Đảm bảo FileUploadException khi destroy() ném IOException
    // Input: publicId = "xyz", uploader.destroy throws IOException
    // Expected: FileUploadException("Error occurred while deleting image: <msg>")
    // ===============================================================
    @Test
    public void testDeleteImage_Failure_IOException() throws Exception {
        doThrow(new IOException("delete error"))
                .when(uploader).destroy("xyz", ObjectUtils.emptyMap());

        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> imageService.deleteImage("xyz"));
        assertTrue(ex.getMessage().contains("Error occurred while deleting image: delete error"));
    }

    // ===============================================================
    // TC-IS-008: Upload thành công file WebP dưới 5MB
    // Mục tiêu: Đảm bảo uploadImage chấp nhận contentType "image/webp"
    // Input: MultipartFile WebP kích thước nhỏ hơn 5MB
    // Expected: ApiResponse.code=200, message="Image uploaded successfully"
    // ===============================================================
    @Test
    public void testUploadImage_Success_Webp() throws Exception {
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.webp", "image/webp", content);

        Map<String,Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "http://cdn/test.webp");
        when(uploader.upload(eq(content), anyMap())).thenReturn(uploadResult);

        ApiResponse<String> resp = imageService.uploadImage(file);

        assertEquals(200, resp.getCode());
        assertEquals("Image uploaded successfully", resp.getMessage());
        assertEquals("http://cdn/test.webp", resp.getResult());
    }

    // ===============================================================
    // TC-IS-009: Upload thất bại khi contentType null
    // Mục tiêu: Đảm bảo FileUploadException khi file.getContentType() == null
    // Input: MultipartFile với contentType = null
    // Expected: FileUploadException("Invalid file type. Only JPEG and PNG are allowed.")
    // ===============================================================
    @Test
    public void testUploadImage_Failure_NullContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "nofmt", null, new byte[10]);
        FileUploadException ex = assertThrows(FileUploadException.class,
                () -> imageService.uploadImage(file));
        assertEquals("Invalid file type. Only JPEG and PNG are allowed.", ex.getMessage());
    }

    // ===============================================================
    // TC-IS-010: Upload thất bại khi Cloudinary trả về map không chứa "url"
    // Mục tiêu: Xem cách service phản ứng khi uploadResult.get("url") == null
    // Input: uploader.upload trả về map rỗng
    // Expected: NullPointerException hoặc FileUploadException do toString() trên null
    // ===============================================================
    @Test
    public void testUploadImage_Failure_MissingUrlInResponse() throws Exception {
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "img.png", "image/png", content);

        // Trả về map không có khóa "url"
        when(uploader.upload(eq(content), anyMap()))
                .thenReturn(Collections.emptyMap());

        // Tùy implementation, có thể là NullPointerException
        assertThrows(NullPointerException.class, () ->
                imageService.uploadImage(file));
    }

}
