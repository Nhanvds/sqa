package com.doan.backend.services;


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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest
@Transactional
public class TestCartService {
    @Spy // dùng @Spy để mock 1 phần CartService
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

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("TC_CART_ADD_01 - Thêm sản phẩm hợp lệ vào giỏ hàng thành công")
    void testAddCartItem_Success() {
        // Given: Thiết lập dữ liệu đầu vào và các giả lập (mock)

        // Tạo CartItemRequest với thông tin giỏ hàng cần thêm sản phẩm
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartId("cart123");
        cartItemRequest.setProductId("product123");
        cartItemRequest.setSizeId("size123");
        cartItemRequest.setQuantity(2);

        // Tạo một ProductInventory giả lập, giả sử đã có sản phẩm với thông tin này
        ProductInventory productInventory = new ProductInventory();
        productInventory.setProduct(new Product());
        productInventory.setSize(new Size());
        productInventory.setQuantity(100);  // Số lượng tồn kho giả lập

        // Tạo Cart giả lập (giỏ hàng đã tồn tại)
        Cart cart = new Cart();
        cart.setId("cart123");

        // Tạo CartItem giả lập (sản phẩm sẽ được thêm vào giỏ)
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(productInventory.getProduct());
        cartItem.setSize(productInventory.getSize());
        cartItem.setQuantity(cartItemRequest.getQuantity());

        // Tạo CartItemResponse giả lập (để trả về từ mapper)
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setId("item123");
        cartItemResponse.setProduct(new ProductResponse());
        cartItemResponse.setSize(new SizeResponse());
        cartItemResponse.setQuantity(cartItem.getQuantity());

        // Giả lập việc tìm kiếm sản phẩm tồn kho từ repository
        when(productInventoryRepository.findByProductIdAndSizeId(anyString(), anyString()))
                .thenReturn(Optional.of(productInventory));

        // Giả lập việc tìm kiếm giỏ hàng từ repository
        when(cartRepository.findById("cart123")).thenReturn(Optional.of(cart));

        // Giả lập việc lưu CartItem mới vào repository
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // Giả lập việc chuyển đổi CartItem thành CartItemResponse
        when(cartItemMapper.toCartItemResponse(any(CartItem.class))).thenReturn(cartItemResponse);

        // When: Thực thi phương thức cần test
        ApiResponse<CartItemResponse> response = cartService.addCartItem(cartItemRequest);

        // Then: Kiểm tra kết quả trả về
        assertEquals(200, response.getCode());  // Kiểm tra mã trả về là 200 (thành công)
        assertEquals("Cart item added successfully", response.getMessage());  // Kiểm tra thông điệp trả về
        assertNotNull(response.getResult());  // Kiểm tra kết quả trả về không null

        // Kiểm tra các trường dữ liệu trong kết quả trả về
        assertEquals("item123", response.getResult().getId());  // Kiểm tra ID sản phẩm
        assertEquals(2, response.getResult().getQuantity());  // Kiểm tra số lượng sản phẩm
    }

    @Test
    @DisplayName("TC_CART_ADD_02 - Không tìm thấy Cart - Throw RuntimeException")
    void testAddCartItem_CartNotFound() {
        // Arrange
        CartItemRequest request = new CartItemRequest();
        request.setCartId("nonExistingCartId");
        request.setProductId("someProductId");
        request.setSizeId("someSizeId");
        request.setQuantity(2);

        when(cartRepository.findById(request.getCartId())).thenReturn(Optional.empty());

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
        // Arrange
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartId("cart123");
        cartItemRequest.setProductId("product123");
        cartItemRequest.setSizeId("size123");
        cartItemRequest.setQuantity(1);

        // Giả lập cartRepository.findById() trả về cart hợp lệ
        Cart cart = new Cart();
        when(cartRepository.findById(cartItemRequest.getCartId())).thenReturn(Optional.of(cart));

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
        // Arrange
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartId("cart123");
        cartItemRequest.setProductId("product123");
        cartItemRequest.setSizeId("size123");
        cartItemRequest.setQuantity(10); // yêu cầu 10 cái

        Cart cart = new Cart(); // giả lập cart

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(5); // chỉ có 5 cái trong kho → không đủ!

        // Giả lập mock các dependency
        when(cartRepository.findById("cart123")).thenReturn(Optional.of(cart));
        // Giả lập private method validateProductInventory bằng cách mock public method hoặc restructure (ví dụ spy)
        CartService spyCartService = cartService;
        doReturn(productInventory).when(spyCartService).validateProductInventory(cartItemRequest);

        // Act & Assert
        assertThatThrownBy(() -> spyCartService.addCartItem(cartItemRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient stock");

        // verify cartRepository đã gọi
        verify(cartRepository, times(1)).findById("cart123");
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
    @DisplayName("TC_CART_UPDATE_01 - Cập nhật cart item thành công (cartItemId hợp lệ + CartItemRequest hợp lệ)")
    void testUpdateCartItem_Success() {
        // Arrange
        String cartItemId = "cartItem123";
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartId("cart123");
        cartItemRequest.setProductId("product123");
        cartItemRequest.setSizeId("size123");
        cartItemRequest.setQuantity(2);

        CartItem existingCartItem = new CartItem();
        existingCartItem.setId(cartItemId);

        ProductInventory productInventory = new ProductInventory();
        Product product = new Product();
        Size size = new Size();
        productInventory.setProduct(product);
        productInventory.setSize(size);

        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(cartItemId);
        updatedCartItem.setProduct(product);
        updatedCartItem.setSize(size);
        updatedCartItem.setQuantity(2);

        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setId(cartItemId);

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
        verify(cartService, times(1)).validateProductInventory(cartItemRequest);
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
        verify(cartService, times(1)).validateProductInventory(cartItemRequest);
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


}
