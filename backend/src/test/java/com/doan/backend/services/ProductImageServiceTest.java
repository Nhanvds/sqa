package com.doan.backend.services;

import com.doan.backend.dto.request.DeleteProductImageRequest;
import com.doan.backend.dto.request.ProductImageRequest;
import com.doan.backend.entity.Product;
import com.doan.backend.entity.ProductImage;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.repositories.ProductImageRepository;
import com.doan.backend.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


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

        productImageService.createProductImage(request);

        assertEquals(2, productImageRepository.findAllByProductId(product.getId()).spliterator().getExactSizeIfKnown());
    }

    // TC02 - Tạo ProductImage với Product không tồn tại
    @Test
    void testCreateProductImage_ProductNotFound_TC02() {
        ProductImageRequest request = new ProductImageRequest();
        request.setIdProduct(UUID.randomUUID().toString());
        request.setImageUrl(List.of("https://image.com"));

        assertThrows(RuntimeException.class, () -> productImageService.createProductImage(request));
    }

    // TC04 - Xóa ProductImage thành công
    @Test
    void testDeleteProductImage_Success_TC04() {
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

        assertFalse(productImageRepository.findById(image.getId()).isPresent());
    }

    // TC06 - Xóa ProductImage với ID không tồn tại
    @Test
    void testDeleteProductImage_NotFound_TC06() {
        assertThrows(RuntimeException.class,() -> {
            DeleteProductImageRequest request = new DeleteProductImageRequest();
            request.setIds(List.of(UUID.randomUUID().toString()));

            productImageService.deleteProductImage(request);
        });

    }

    // TC07 - Lấy danh sách ProductImage theo ProductId hợp lệ
    @Test
    void testGetProductImagesByProductId_Success_TC07() {
        assertDoesNotThrow(() -> {
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
            assertEquals(2, ((List<?>) response.getResult()).size());
        }
        );
    }

    // TC08 - Lấy danh sách ProductImage theo ProductId không có image
    @Test
    void testGetProductImagesByProductId_Empty_TC08() {
        assertDoesNotThrow(() -> {

            Product product = new Product();
            product.setName("Product Empty");
            product.setDescription("Description");
            product.setPrice(BigDecimal.valueOf(200));
            product.setStatus(StatusEnum.ACTIVE);
            product = productRepository.save(product);

            var response = productImageService.getProductImagesByProductId(product.getId());
            assertFalse(response.getResult().iterator().hasNext());
        });
    }

    // TC03 - Tạo ProductImage với danh sách URL rỗng
    @Test
    void testCreateProductImage_EmptyURL_TC03() {
        assertDoesNotThrow(() -> {

            Product product = new Product();
            product.setName("Product Test");
            product.setDescription("Description");
            product.setPrice(BigDecimal.valueOf(300));
            product.setStatus(StatusEnum.ACTIVE);
            product = productRepository.save(product);

            ProductImageRequest request = new ProductImageRequest();
            request.setIdProduct(product.getId());
            request.setImageUrl(new ArrayList<>());

            productImageService.createProductImage(request);
            assertFalse(productImageRepository.findAllByProductId(product.getId()).iterator().hasNext());
        });
    }

    // TC05 - Xóa ProductImage với danh sách ID rỗng
    @Test
    void testDeleteProductImage_EmptyIdList_TC05() {
        assertDoesNotThrow(() -> {

            DeleteProductImageRequest request = new DeleteProductImageRequest();
            request.setIds(new ArrayList<>());

            Integer check1 = productImageRepository.findAll().size();

            var response = productImageService.deleteProductImage(request);
//            assertFalse(response.getResult().iterator().hasNext());

            assertEquals(check1, productImageRepository.findAll().size());
        });
    }

    // TC09 - Lấy ProductImage với ProductId không tồn tại
    @Test
    void testGetProductImagesByProductId_ProductNotFound_TC09() {
        assertDoesNotThrow(() -> {

            var response = productImageService.getProductImagesByProductId(UUID.randomUUID().toString());
            assertFalse(response.getResult().iterator().hasNext());
        });
    }

}
