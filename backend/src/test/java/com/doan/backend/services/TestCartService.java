package com.doan.backend.services;


import com.doan.backend.DummyDataFactory;
import com.doan.backend.dto.request.CartItemRequest;
import com.doan.backend.dto.response.*;
import com.doan.backend.entity.*;
import com.doan.backend.mapper.CartItemMapper;
import com.doan.backend.mapper.CartMapper;
import com.doan.backend.repositories.CartItemRepository;
import com.doan.backend.repositories.CartRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.doan.backend.repositories.ProductInventoryRepository;
import com.doan.backend.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestCartService {
    @InjectMocks
    private CartService cartService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductInventoryRepository productInventoryRepository;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private CartMapper cartMapper;



    @Test
    @DisplayName("TC_CART_ADD_01 - Thêm sản phẩm hợp lệ vào giỏ hàng thành công")
    void testAddCartItem_Success() {
        // Given: Thiết lập dữ liệu đầu vào và các giả lập (mock)
        CartItemRequest cartItemRequest = DummyDataFactory.dummyCartItemRequest();
        ProductInventory productInventory = DummyDataFactory.dummyProductInventory();
        Cart cart = DummyDataFactory.dummyCart();
        cartItemRequest.setCartId(cart.getId());
        CartItem cartItem = DummyDataFactory.dummyCartItem();
        CartItemResponse cartItemResponse = DummyDataFactory.dummyCartItemResponse();

        // Giả lập việc tìm kiếm sản phẩm tồn kho từ repository
        Mockito.when(productInventoryRepository.findByProductIdAndSizeId(anyString(), anyString()))
                .thenReturn(Optional.of(productInventory));

        // Giả lập việc tìm kiếm giỏ hàng từ repository
        Mockito.when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        // Giả lập việc lưu CartItem mới vào repository
        Mockito.when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // Giả lập việc chuyển đổi CartItem thành CartItemResponse
        Mockito.when(cartItemMapper.toCartItemResponse(any(CartItem.class))).thenReturn(cartItemResponse);

        // When: Thực thi phương thức cần test
        ApiResponse<CartItemResponse> response = cartService.addCartItem(cartItemRequest);

        // Then: Kiểm tra kết quả trả về
        assertEquals(200, response.getCode());  // Kiểm tra mã trả về là 200 (thành công)
        assertEquals("Cart item added successfully", response.getMessage());  // Kiểm tra thông điệp trả về
        assertNotNull(response.getResult());  // Kiểm tra kết quả trả về không null

        // Kiểm tra các trường dữ liệu trong kết quả trả về
        assertEquals(cartItemResponse.getId(), response.getResult().getId());  // Kiểm tra ID sản phẩm
        assertEquals(cartItemResponse.getQuantity(), response.getResult().getQuantity());  // Kiểm tra số lượng sản phẩm
    }

    @Test
    @DisplayName("TC_CART_ADD_02 - Không tìm thấy Cart - Throw RuntimeException")
    void testAddCartItem_CartNotFound() {
        // Arrange
        CartItemRequest request = DummyDataFactory.dummyCartItemRequest();
        ProductInventory productInventory = DummyDataFactory.dummyProductInventory();
        when(cartRepository.findById(request.getCartId())).thenReturn(Optional.empty());
        when(productInventoryRepository.findByProductIdAndSizeId(anyString(), anyString()))
                .thenReturn(Optional.of(productInventory));
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addCartItem(request);
        });
        assertEquals("Cart not found", exception.getMessage());

        verify(cartRepository, times(1)).findById(request.getCartId());
        verifyNoMoreInteractions(cartRepository, cartItemRepository, cartItemMapper);
    }

    @Test
    @DisplayName("TC_CART_ADD_03 - Không tìm thấy ProductInventory")
    void testAddCartItem_ProductInventoryNotFound_ShouldThrowException() {
        // Given: Thiết lập dữ liệu đầu vào và các giả lập (mock)
        CartItemRequest cartItemRequest = DummyDataFactory.dummyCartItemRequest();
        // Giả lập productInventoryRepository.findByProductIdAndSizeId() trả về Optional.empty()
        when(productInventoryRepository.findByProductIdAndSizeId(
                cartItemRequest.getProductId(), cartItemRequest.getSizeId()))
                .thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            cartService.addCartItem(cartItemRequest);
        });

        assertThat(thrown.getMessage()).isEqualTo("Product inventory not found");
    }

    @Test
    @DisplayName("TC_CART_ADD_04 - Kiểm tra số lượng tồn kho không đủ (Insufficient stock)")
    void testAddCartItem_InsufficientStock_ShouldThrowRuntimeException() {
        // Given: Thiết lập dữ liệu đầu vào và các giả lập (mock)
        CartItemRequest cartItemRequest = DummyDataFactory.dummyCartItemRequest();
        cartItemRequest.setQuantity(200); // lớn hơn 100 trong productinventory
        ProductInventory productInventory = DummyDataFactory.dummyProductInventory();
        Cart cart = DummyDataFactory.dummyCart();
        cartItemRequest.setCartId(cart.getId());

        // Giả lập mock các dependency
        when(productInventoryRepository.findByProductIdAndSizeId(anyString(), anyString()))
                .thenReturn(Optional.of(productInventory));
        // Act & Assert
        assertThatThrownBy(() -> cartService.addCartItem(cartItemRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");

    }

    @Test
    @DisplayName("TC_CART_GET_01 - Lấy giỏ hàng thành công khi đã tồn tại (userId hợp lệ có giỏ hàng)")
    void testGetCartByUserId_Success() {
        // Arrange
        String userId = "user123";
        Cart cart = new Cart();
        cart.setId("cart123");

        CartItem cartItem = new CartItem();
        cartItem.setId("cartItem123");

        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setId("cartItem123");

        CartResponse cartResponse = new CartResponse();
        cartResponse.setId("cart123");

        // Mock repository trả ra Cart entity
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Mock repository trả ra list CartItem entity
        when(cartItemRepository.findByCartId("cart123")).thenReturn(List.of(cartItem));

        // Mock mapper chuyển Cart entity -> CartResponse
        when(cartMapper.toCartResponse(cart)).thenReturn(cartResponse);

        // Mock mapper chuyển list CartItem entity -> list CartItemResponse
        when(cartItemMapper.tocartItemResponseList(List.of(cartItem))).thenReturn(List.of(cartItemResponse));

        // Act
        ApiResponse<CartResponse> actualResponse = cartService.getCartByUserId(userId);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(200);
        assertThat(actualResponse.getMessage()).isEqualTo("get cart successfully");
        assertThat(actualResponse.getResult()).isNotNull();
        assertThat(actualResponse.getResult().getId()).isEqualTo("cart123");
        assertThat(actualResponse.getResult().getCartItems()).hasSize(1);
        assertThat(actualResponse.getResult().getCartItems().get(0).getId()).isEqualTo("cartItem123");

        // Verify gọi đúng
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartItemRepository, times(1)).findByCartId("cart123");
        verify(cartMapper, times(1)).toCartResponse(cart);
        verify(cartItemMapper, times(1)).tocartItemResponseList(List.of(cartItem));
    }


    @Test
    @DisplayName("TC_CART_GET_02 - Tạo mới giỏ hàng nếu chưa tồn tại (userId hợp lệ chưa có giỏ hàng)")
    void testGetCartByUserId_CreateNewCart() {
        // Arrange
        String userId = "user123";

        // Mock user (vì createCart cần user)
        User user = new User();
        user.setId(userId);

        // Giả lập trường hợp không có giỏ hàng với userId này
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Mock userRepository.findById
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Mock tạo cart mới
        Cart newCart = new Cart();
        newCart.setId("cart123");
        newCart.setUser(user);

        // Khi save Cart, trả về newCart
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        // CartResponse giả
        CartResponse newCartResponse = new CartResponse();
        newCartResponse.setId("cart123");

        // Mock cartMapper
        when(cartMapper.toCartResponse(newCart)).thenReturn(newCartResponse);

        // Act
        ApiResponse<CartResponse> actualResponse = cartService.getCartByUserId(userId);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(200);
        assertThat(actualResponse.getMessage()).isEqualTo("get cart successfully");
        assertThat(actualResponse.getResult()).isNotNull();
        assertThat(actualResponse.getResult().getId()).isEqualTo("cart123");
        assertThat(actualResponse.getResult().getCartItems()).isNullOrEmpty(); // Giỏ hàng mới tạo sẽ không có items

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartMapper, times(1)).toCartResponse(newCart);
    }


    @Test
    @DisplayName("TC_CART_GET_03 - Không tìm thấy người dùng (userId không tồn tại)")
    void testGetCartByUserId_UserNotFound_ShouldThrowException() {
        // Arrange
        String userId = "nonexistentUser";

        // Giả lập không có cart
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Giả lập userId không tồn tại trong DB
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.getCartByUserId(userId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("User not found with id: " + userId);

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartMapper, never()).toCartResponse(any());
    }

    @Test
    @DisplayName("TC_CART_UPDATE_01 - Cập nhật cart item thành công (cartItemId hợp lệ + CartItemRequest hợp lệ)")
    void testUpdateCartItem_Success() {
        // Given: Thiết lập dữ liệu đầu vào và các giả lập (mock)
        CartItemRequest cartItemRequest = DummyDataFactory.dummyCartItemRequest();
        ProductInventory productInventory = DummyDataFactory.dummyProductInventory();
        Cart cart = DummyDataFactory.dummyCart();
        cartItemRequest.setCartId(cart.getId());
        CartItem existingCartItem = DummyDataFactory.dummyCartItem();
        String cartItemId = existingCartItem.getId();
        CartItemResponse cartItemResponse = DummyDataFactory.dummyCartItemResponse();
        cartItemResponse.setId(cartItemId);
        CartItem updatedCartItem = DummyDataFactory.dummyCartItem();
        updatedCartItem.setId(cartItemId);
        // Mock behavior
        when(productInventoryRepository.findByProductIdAndSizeId(
                cartItemRequest.getProductId(),
                cartItemRequest.getSizeId()
        )).thenReturn(Optional.of(productInventory));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(existingCartItem)).thenReturn(updatedCartItem);
        when(cartItemMapper.toCartItemResponse(updatedCartItem)).thenReturn(cartItemResponse);

        // Act
        ApiResponse<CartItemResponse> actualResponse = cartService.updateCartItem(cartItemId, cartItemRequest);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(200);
        assertThat(actualResponse.getMessage()).isEqualTo("Cart item updated successfully");
        assertThat(actualResponse.getResult()).isNotNull();
        assertThat(actualResponse.getResult().getId()).isEqualTo(cartItemId);

        // Verify behavior
        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, times(1)).save(existingCartItem);
        verify(cartItemMapper, times(1)).toCartItemResponse(updatedCartItem);
    }

    @Test
    @DisplayName("TC_CART_UPDATE_02 - Không tìm thấy CartItem (cartItemId không tồn tại)")
    void testUpdateCartItem_CartItemNotFound() {
        // Arrange
        String cartItemId = "nonExistingCartItemId";
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartId("cart123");
        cartItemRequest.setProductId("product123");
        cartItemRequest.setSizeId("size123");
        cartItemRequest.setQuantity(2);

        ProductInventory productInventory = new ProductInventory();
        Product product = new Product();
        Size size = new Size();
        productInventory.setProduct(product);
        productInventory.setSize(size);
        productInventory.setQuantity(10);

        // Mock behavior
        when(productInventoryRepository.findByProductIdAndSizeId(
                cartItemRequest.getProductId(),
                cartItemRequest.getSizeId()
        )).thenReturn(Optional.of(productInventory));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty()); // không tìm thấy

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItem(cartItemId, cartItemRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("Cart item not found");

        // Verify behavior
        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, never()).save(any());
        verify(cartItemMapper, never()).toCartItemResponse(any());
    }

    @Test
    @DisplayName("TC_CART_UPDATE_03 - Không tìm thấy ProductInventory (ProductId/SizeId không tồn tại)")
    void testUpdateCartItem_ProductInventoryNotFound() {
        // Arrange
        String cartItemId = "cartItem123";
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartId("cart123");
        cartItemRequest.setProductId("nonExistingProductId");
        cartItemRequest.setSizeId("nonExistingSizeId");
        cartItemRequest.setQuantity(2);

        // Mock productInventoryRepository để validateProductInventory() ném lỗi
        when(productInventoryRepository.findByProductIdAndSizeId(
                cartItemRequest.getProductId(),
                cartItemRequest.getSizeId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItem(cartItemId, cartItemRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("Product inventory not found");

        // Verify behavior
        verify(productInventoryRepository, times(1))
                .findByProductIdAndSizeId(cartItemRequest.getProductId(), cartItemRequest.getSizeId());
        verify(cartItemRepository, never()).findById(any());
        verify(cartItemRepository, never()).save(any());
        verify(cartItemMapper, never()).toCartItemResponse(any());
    }

    @Test
    @DisplayName("TC_CART_UPDATE_04 - Số lượng vượt quá tồn kho (báo lỗi và không thay đổi dữ liệu)")
    void testUpdateCartItem_QuantityExceedsStock() {
        // Arrange
        String cartItemId = "cartItem123";
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartId("cart123");
        cartItemRequest.setProductId("product123");
        cartItemRequest.setSizeId("sizeM");
        cartItemRequest.setQuantity(10); // Yêu cầu 10 sản phẩm

        // Giả lập tồn kho hiện tại chỉ có 5 sản phẩm
        ProductInventory productInventory = new ProductInventory();
        productInventory.setProduct(new Product());
        productInventory.getProduct().setName("Áo thun");
        productInventory.setSize(new Size());
        productInventory.setQuantity(5); // Chỉ có 5 trong kho

        when(productInventoryRepository.findByProductIdAndSizeId(
                cartItemRequest.getProductId(),
                cartItemRequest.getSizeId()))
                .thenReturn(Optional.of(productInventory));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItem(cartItemId, cartItemRequest);
        });

        // Assert nội dung thông báo lỗi
        assertThat(exception.getMessage()).isEqualTo("Insufficient stock for product: Áo thun");

        // Verify không thực hiện các thao tác sau
        verify(cartItemRepository, never()).findById(any());
        verify(cartItemRepository, never()).save(any());
        verify(cartItemMapper, never()).toCartItemResponse(any());
    }

    @Test
    @DisplayName("TC_CART_DELETE_01 - Xóa cart item thành công (cartItemId hợp lệ)")
    void testDeleteCartItem_Success() {
        // Arrange
        String cartItemId = "cartItem123";

        // Act
        ApiResponse<Void> actualResponse = cartService.deleteCartItem(cartItemId);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(200);
        assertThat(actualResponse.getMessage()).isEqualTo("delete cart item successfully");
        assertThat(actualResponse.getResult()).isNull();

        // Verify behavior
        verify(cartItemRepository, times(1)).deleteById(cartItemId);
    }

    @Test
    @DisplayName("TC_CART_DELETE_02 - Xóa sản phẩm không tồn tại (cartItemId không tồn tại)")
    void testDeleteCartItem_CartItemNotFound() {
        // Arrange
        String nonExistingCartItemId = "nonExistingCartItemId";

        // Mock behavior
        when(cartItemRepository.existsById(nonExistingCartItemId)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.deleteCartItem(nonExistingCartItemId);
        });

        assertThat(exception.getMessage()).isEqualTo("Cart item not found");

        // Verify behavior
        verify(cartItemRepository, never()).deleteById(anyString());
    }



}
