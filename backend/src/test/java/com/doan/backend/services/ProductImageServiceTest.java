package com.doan.backend.services;

import com.doan.backend.dto.request.DeleteProductImageRequest;
import com.doan.backend.dto.request.ProductImageRequest;
import com.doan.backend.entity.Product;
import com.doan.backend.entity.ProductImage;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.repositories.ProductImageRepository;
import com.doan.backend.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Transactional
public class ProductImageServiceTest {

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    // TC01 - Tạo ProductImage với Product hợp lệ
    @Test
    void testCreateProductImage_Success_TC01() {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStatus(StatusEnum.ACTIVE);
        product = productRepository.save(product);

        ProductImageRequest request = new ProductImageRequest();
        request.setIdProduct(product.getId());
        List<String> urls = new ArrayList<>();
        urls.add("https://image1.com");
        urls.add("https://image2.com");
        request.setImageUrl(urls);

        var response = productImageService.createProductImage(request);

        Assertions.assertEquals(200, response.getCode());
        Assertions.assertEquals(2, productImageRepository.findAllByProductId(product.getId()).spliterator().getExactSizeIfKnown());
    }

    // TC02 - Tạo ProductImage với Product không tồn tại
    @Test
    void testCreateProductImage_ProductNotFound_TC02() {
        ProductImageRequest request = new ProductImageRequest();
        request.setIdProduct(UUID.randomUUID().toString());
        request.setImageUrl(List.of("https://image.com"));

        Assertions.assertThrows(RuntimeException.class, () -> productImageService.createProductImage(request));
    }

    // TC03 - Xóa ProductImage thành công
    @Test
    void testDeleteProductImage_Success_TC03() {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStatus(StatusEnum.ACTIVE);
        product = productRepository.save(product);

        ProductImage image = new ProductImage();
        image.setImageUrl("https://image.com");
        image.setProduct(product);
        image = productImageRepository.save(image);

        DeleteProductImageRequest request = new DeleteProductImageRequest();
        request.setIds(List.of(image.getId()));

        var response = productImageService.deleteProductImage(request);

        Assertions.assertEquals(200, response.getCode());
        Assertions.assertFalse(productImageRepository.findById(image.getId()).isPresent());
    }

    // TC04 - Xóa ProductImage với ID không tồn tại
    @Test
    void testDeleteProductImage_NotFound_TC04() {
        DeleteProductImageRequest request = new DeleteProductImageRequest();
        request.setIds(List.of(UUID.randomUUID().toString()));

        var response = productImageService.deleteProductImage(request);
        Assertions.assertEquals(200, response.getCode());
    }

    // TC05 - Lấy danh sách ProductImage theo ProductId hợp lệ
    @Test
    void testGetProductImagesByProductId_Success_TC05() {
        Product product = new Product();
        product.setName("Product A");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(200));
        product.setStatus(StatusEnum.ACTIVE);
        product = productRepository.save(product);

        ProductImage image1 = new ProductImage(null, product, "https://image1.com");
        ProductImage image2 = new ProductImage(null, product, "https://image2.com");
        productImageRepository.save(image1);
        productImageRepository.save(image2);

        var response = productImageService.getProductImagesByProductId(product.getId());
        Assertions.assertEquals(200, response.getCode());
        Assertions.assertEquals(2, ((List<?>) response.getResult()).size());
    }

    // TC06 - Lấy danh sách ProductImage theo ProductId không có image
    @Test
    void testGetProductImagesByProductId_Empty_TC06() {
        Product product = new Product();
        product.setName("Product Empty");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(200));
        product.setStatus(StatusEnum.ACTIVE);
        product = productRepository.save(product);

        var response = productImageService.getProductImagesByProductId(product.getId());
        Assertions.assertEquals(200, response.getCode());
        Assertions.assertFalse(response.getResult().iterator().hasNext());
    }

    // TC07 - Tạo ProductImage với danh sách URL rỗng
    @Test
    void testCreateProductImage_EmptyURL_TC07() {
        Product product = new Product();
        product.setName("Product Test");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(300));
        product.setStatus(StatusEnum.ACTIVE);
        product = productRepository.save(product);

        ProductImageRequest request = new ProductImageRequest();
        request.setIdProduct(product.getId());
        request.setImageUrl(new ArrayList<>());

        var response = productImageService.createProductImage(request);
        Assertions.assertEquals(200, response.getCode());
        Assertions.assertFalse(productImageRepository.findAllByProductId(product.getId()).iterator().hasNext());
    }

    // TC08 - Xóa ProductImage với danh sách ID rỗng
    @Test
    void testDeleteProductImage_EmptyIdList_TC08() {
        DeleteProductImageRequest request = new DeleteProductImageRequest();
        request.setIds(new ArrayList<>());

        var response = productImageService.deleteProductImage(request);
        Assertions.assertEquals(200, response.getCode());
    }

    // TC09 - Lấy ProductImage với ProductId không tồn tại
    @Test
    void testGetProductImagesByProductId_ProductNotFound_TC09() {
        var response = productImageService.getProductImagesByProductId(UUID.randomUUID().toString());
        Assertions.assertEquals(200, response.getCode());
        Assertions.assertFalse(response.getResult().iterator().hasNext());
    }

    // TC10 - Tạo ProductImage với null ID Product
    @Test
    void testCreateProductImage_NullProductId_TC10() {
        ProductImageRequest request = new ProductImageRequest();
        request.setIdProduct(null);
        request.setImageUrl(List.of("https://image.com"));

        Assertions.assertThrows(RuntimeException.class, () -> productImageService.createProductImage(request));
    }
}
