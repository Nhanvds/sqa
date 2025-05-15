package com.doan.backend.services;

import com.doan.backend.dto.request.OrderRequest;
import com.doan.backend.dto.request.UpdateOrderRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.OrderResponse;
import com.doan.backend.dto.response.ShippingAddressResponse;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.entity.*;
import com.doan.backend.enums.DiscountType;
import com.doan.backend.enums.InvoiceStatusEnum;
import com.doan.backend.enums.OrderStatusEnum;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.OrderMapper;
import com.doan.backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestOrderService {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductInventoryRepository productInventoryRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private DiscountRepository discountRepository;
    @Mock
    private ShippingAddressRepository shippingAddressRepository;
    @Mock
    private PromotionProductRepository promotionProductRepository;
    @Mock
    private PromotionService promotionService;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private UserDiscountRepository userDiscountRepository;

    @InjectMocks
    private OrderService orderService;
    // Dữ liệu mẫu dùng chung
    private User user;
    private ShippingAddress shippingAddress;
    private Product product;
    private Size size;
    private ProductInventory productInventory;
    private Cart cart;
    private CartItem cartItem;
    private Discount discount;

    @BeforeEach
    void setup() {


        // Nếu dùng @InjectMocks orderService thì OrderMapper sẽ được inject mock

        // Khởi tạo người dùng
        user = new User();
        user.setId("user-001");
        user.setEmail("johndoe@example.com");

        // Địa chỉ giao hàng
        shippingAddress = new ShippingAddress();
        shippingAddress.setId("ship-001");
        shippingAddress.setUser(user);
        shippingAddress.setAddressDetail("123 Main Street");
        shippingAddress.setCity("Hanoi");

        // Sản phẩm
        product = new Product();
        product.setId("prod-001");
        product.setName("Áo thun nam thể thao");
        product.setPrice(new BigDecimal("250000")); // 250.000 VNĐ
        product.setStatus(StatusEnum.ACTIVE);

        // Kích cỡ sản phẩm
        size = new Size();
        size.setId("size-M");
        size.setName("M");

        // Tồn kho sản phẩm
        productInventory = new ProductInventory();
        productInventory.setId("inv-001");
        productInventory.setProduct(product);
        productInventory.setSize(size);
        productInventory.setQuantity(50);

        // Giỏ hàng của người dùng
        cart = new Cart();
        cart.setId("cart-001");
        cart.setUser(user);

        // Mục trong giỏ hàng
        cartItem = new CartItem();
        cartItem.setId("item-001");
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(2); // 2 chiếc áo size M

        // Mã giảm giá
        discount = new Discount();
        discount.setId("discount-001");
        discount.setCode("SUMMER2025");
        discount.setDiscountType(DiscountType.PERCENTAGE); // hoặc DiscountType.VALUE, tùy logic
        discount.setDiscountPercentage(new BigDecimal("10")); // 10%
        discount.setDiscountValue(null); // nếu dùng theo phần trăm
        discount.setMaxDiscountValue(new BigDecimal("50000")); // tối đa giảm 50k
        discount.setMinOrderValue(new BigDecimal("400000")); // áp dụng từ 400k
        discount.setMaxUses(100); // tổng số lượt được dùng
        discount.setUsedCount(0); // chưa ai dùng
        discount.setStartDate(LocalDateTime.now().minusDays(3));
        discount.setExpiryDate(LocalDateTime.now().plusDays(10));
        discount.setAutoApply(false);
    }


    @Test
    @DisplayName("TC_ORDER_001 - Cart không tồn tại")
    void testCreateOrderFromCart_CartNotFound() {
        when(cartRepository.findByUserId("user-001")).thenReturn(Optional.empty());

        OrderRequest request = new OrderRequest();
        request.setUserId("user-001");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.createOrderFromCart(request));

        assertEquals("Cart not found", exception.getMessage());
    }

    @Test
    @DisplayName("TC_ORDER_002 - Địa chỉ giao hàng không tồn tại (shippingAddressId không tồn tại)")
    void TC_ORDER_002_shippingAddressNotFound_shouldThrowException() {
        // Arrange
        String userId = "user-001";
        String invalidShippingAddressId = "invalid-ship-999";
        OrderRequest request = new OrderRequest();
        request.setUserId(userId);
        request.setShippingAddressId(invalidShippingAddressId);

        // Mock cart tồn tại
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Mock địa chỉ giao hàng KHÔNG tồn tại
        when(shippingAddressRepository.findById(invalidShippingAddressId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(request);
        });

        assertEquals("Shipping address not found", exception.getMessage());

        // Verify các hành động không được thực hiện
        verify(cartItemRepository, never()).findByCartId(any());
        verify(orderRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }


    @Test
    @DisplayName("TC_ORDER_003 - Product inventory không tồn tại (productInventoryId không tồn tại)")
    void TC_ORDER_003_productInventoryNotFound_shouldThrowException() {
        // Arrange
        String userId = "user-001";
        String shippingAddressId = "address-001";
        String productId = "product-001";
        String sizeId = "size-001";

        OrderRequest request = new OrderRequest();
        request.setUserId(userId);
        request.setShippingAddressId(shippingAddressId);

        // Giả lập cart và shipping address tồn tại
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));

        // Tạo CartItem với Product và Size
        Product product = new Product();
        product.setId(productId);

        Size size = new Size();
        size.setId(sizeId);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(2);

        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));

        // Mock productInventoryRepository trả về empty (không tìm thấy tồn kho)
        when(productInventoryRepository.findByProductIdAndSizeId(productId, sizeId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(request);
        });

        assertEquals("Product inventory not found", exception.getMessage());

        // Đảm bảo không có dữ liệu nào bị ghi
        verify(orderRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }


    @Test
    @DisplayName("TC_ORDER_004 - Không đủ tồn kho (quantity > availableStock)")
    void TC_ORDER_004_insufficientStock_shouldThrowException() {
        // Arrange
        String userId = "user-001";
        String shippingAddressId = "address-001";
        String productId = "product-001";
        String sizeId = "size-001";

        OrderRequest request = new OrderRequest();
        request.setUserId(userId);
        request.setShippingAddressId(shippingAddressId);

        // Giả lập cart và địa chỉ giao hàng tồn tại
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));

        // Tạo CartItem với Product và Size
        Product product = new Product();
        product.setId(productId);

        Size size = new Size();
        size.setId(sizeId);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(10); // Yêu cầu số lượng 10

        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));

        // Giả lập tồn kho chỉ còn 5 cái
        ProductInventory productInventory = new ProductInventory();
        productInventory.setProduct(product);
        productInventory.setSize(size);
        productInventory.setQuantity(5);

        when(productInventoryRepository.findByProductIdAndSizeId(productId, sizeId)).thenReturn(Optional.of(productInventory));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(request);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock for product"));

        // Đảm bảo không có dữ liệu nào bị ghi
        verify(orderRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }


    @Test
    @DisplayName("TC_ORDER_005 - Mã giảm giá đã được sử dụng (discountId đã được used)")
    void TC_ORDER_005_discountAlreadyUsed_shouldThrowException() {
        // Arrange
        String userId = "user-001";
        String shippingAddressId = "address-001";
        String discountId = "discount-001";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        Cart cart = new Cart();
        cart.setId("cart-001");
        User user = new User();
        user.setId(userId);
        cart.setUser(user);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(shippingAddressId);

        Product product = new Product();
        product.setId("prod-001");
        product.setName("Product 1");
        product.setPrice(new BigDecimal("100"));

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(new Size());
        cartItem.setQuantity(2);

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(10);

        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setMaxUses(10);
        discount.setUsedCount(5);
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setExpiryDate(LocalDateTime.now().plusDays(1));
        discount.setDiscountPercentage(new BigDecimal("10"));
        discount.setMaxDiscountValue(new BigDecimal("50"));
        discount.setDiscountType(DiscountType.PERCENTAGE);
        discount.setMinOrderValue(new BigDecimal("0"));

        UserDiscount userDiscount = new UserDiscount();
        userDiscount.setUsesCount(1);  // Đã dùng 1 lần
        userDiscount.setDiscount(discount);
        userDiscount.setUser(user);

        Promotion promotion = new Promotion();
        promotion.setDiscountPercentage(new BigDecimal("10")); // Cần thiết để tránh null

        // Mocks
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productInventoryRepository.findByProductIdAndSizeId(product.getId(), cartItem.getSize().getId()))
                .thenReturn(Optional.of(productInventory));
        when(userDiscountRepository.findByUserIdAndDiscount_Id(userId, discountId)).thenReturn(Optional.of(userDiscount));

        // Mock promotionService trả về giá hợp lệ (không null)
        when(promotionService.applyPromotionToProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return p.getPrice();
        });

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        assertEquals("Discount has been used", exception.getMessage());

        // Verify order không được lưu
        verify(orderRepository, never()).save(any());
    }





    @Test
    @DisplayName("TC_ORDER_006 - Mã giảm giá hết lượt sử dụng (discount.quantity == 0)")
    void TC_ORDER_006_discountQuantityZero_shouldThrowException() {
        // Arrange
        String userId = "user-002";
        String shippingAddressId = "address-002";
        String discountId = "discount-002";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        Cart cart = new Cart();
        cart.setId("cart-002");
        User user = new User();
        user.setId(userId);
        cart.setUser(user);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(shippingAddressId);

        Product product = new Product();
        product.setId("prod-002");
        product.setName("Product 2");
        product.setPrice(new BigDecimal("150"));

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(new Size());
        cartItem.setQuantity(1);

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(5);

        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setMaxUses(10);
        discount.setUsedCount(10);  // Đã dùng hết lượt (dùng tối đa)
        discount.setStartDate(LocalDateTime.now().minusDays(10));
        discount.setExpiryDate(LocalDateTime.now().plusDays(10));
        discount.setDiscountPercentage(new BigDecimal("15"));
        discount.setMaxDiscountValue(new BigDecimal("100"));
        discount.setDiscountType(DiscountType.PERCENTAGE);
        discount.setMinOrderValue(new BigDecimal("0"));

        Promotion promotion = new Promotion();
        promotion.setDiscountPercentage(new BigDecimal("15"));

        // Mocks
        when(cartRepository.findByUserId(eq(orderRequest.getUserId()))).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productInventoryRepository.findByProductIdAndSizeId(product.getId(), cartItem.getSize().getId()))
                .thenReturn(Optional.of(productInventory));
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));


        // Mock promotionService trả về giá trị gốc tránh lỗi null
        when(promotionService.applyPromotionToProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return p.getPrice();
        });

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        assertEquals("Discount are out of stock", exception.getMessage());

        // Verify order không được lưu
        verify(orderRepository, never()).save(any());
    }


    @Test
    @DisplayName("TC_ORDER_007 - Mã giảm giá chưa tới hạn hoặc đã hết hạn (currentDate not in [startDate, expiryDate])")
    void TC_ORDER_007_discountInvalidDate_shouldThrowException() {
        // Arrange
        String userId = "user-003";
        String shippingAddressId = "address-003";
        String discountId = "discount-003";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        Cart cart = new Cart();
        cart.setId("cart-003");
        User user = new User();
        user.setId(userId);
        cart.setUser(user);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(shippingAddressId);

        Product product = new Product();
        product.setId("prod-003");
        product.setName("Product 3");
        product.setPrice(new BigDecimal("100"));

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(new Size());
        cartItem.setQuantity(1);

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(10);

        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setMaxUses(10);
        discount.setUsedCount(0);
        // Test trường hợp chưa tới hạn (startDate trong tương lai)
        discount.setStartDate(LocalDateTime.now().plusDays(5));
        discount.setExpiryDate(LocalDateTime.now().plusDays(10));
        discount.setDiscountPercentage(new BigDecimal("20"));
        discount.setMaxDiscountValue(new BigDecimal("50"));
        discount.setDiscountType(DiscountType.PERCENTAGE);
        discount.setMinOrderValue(new BigDecimal("0"));

        Promotion promotion = new Promotion();
        promotion.setDiscountPercentage(new BigDecimal("20"));

        // Mocks
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productInventoryRepository.findByProductIdAndSizeId(product.getId(), cartItem.getSize().getId()))
                .thenReturn(Optional.of(productInventory));
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));

        // Mock promotionService trả về giá hợp lệ
        when(promotionService.applyPromotionToProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return p.getPrice();
        });

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        assertEquals("Discount is not yet valid", exception.getMessage());

        // Test trường hợp hết hạn (expiryDate trong quá khứ)
        discount.setStartDate(LocalDateTime.now().minusDays(10));
        discount.setExpiryDate(LocalDateTime.now().minusDays(1));

        // Lặp lại assertThrows
        exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        assertEquals("Discount is not yet valid", exception.getMessage());  // hoặc "Discount has expired" tùy message bạn dùng

        // Verify order không được lưu
        verify(orderRepository, never()).save(any());
    }


    @Test
    @DisplayName("TC_ORDER_008 - Tổng tiền không đạt điều kiện discount (cartTotal < discount.minOrderValue)")
    void TC_ORDER_008_cartTotalLessThanMinOrderValue_shouldNotApplyDiscount() {
        // Arrange
        String userId = "user-004";
        String shippingAddressId = "address-004";
        String discountId = "discount-004";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        Cart cart = new Cart();
        cart.setId("cart-004");
        User user = new User();
        user.setId(userId);
        cart.setUser(user);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(shippingAddressId);

        Product product = new Product();
        product.setId("prod-004");
        product.setName("Product 4");
        product.setPrice(new BigDecimal("50"));  // Giá thấp

        Size size = new Size();
        size.setId("size-004");

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(1);

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(10);

        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setMaxUses(10);
        discount.setUsedCount(0);
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setExpiryDate(LocalDateTime.now().plusDays(1));
        discount.setDiscountPercentage(new BigDecimal("20"));  // 20%
        discount.setMaxDiscountValue(new BigDecimal("10"));
        discount.setDiscountType(DiscountType.PERCENTAGE);
        discount.setMinOrderValue(new BigDecimal("100")); // Điều kiện minOrderValue là 100

        // Mocks
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("order-123"); // giả lập DB insert id
            return o;
        });
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productInventoryRepository.findByProductIdAndSizeId(product.getId(), size.getId()))
                .thenReturn(Optional.of(productInventory));
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));
        when(promotionProductRepository.findPromotionApplyByProductId(eq(product.getId()), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            if (inv.getId() == null) {
                inv.setId("invoice-001"); // Giả lập id đã được tạo khi lưu
            }
            return inv;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Mock promotionService trả về giá gốc (không giảm)
        when(promotionService.applyPromotionToProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return p.getPrice();
        });
        // Mock trả về OrderResponse giả lập
        when(orderMapper.toOrderResponse(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);

            OrderResponse response = new OrderResponse();
            response.setTotalPriceBeforeDiscount(order.getTotalPriceBeforeDiscount());
            response.setTotalPriceAfterDiscount(order.getTotalPriceAfterDiscount());
            // Nếu cần, map thêm các trường khác

            return response;
        });


        // Mock userDiscountRepository trả về empty (user chưa dùng discount)
        when(userDiscountRepository.findByUserIdAndDiscount_Id(userId, discountId)).thenReturn(Optional.empty());

        // Act
        ApiResponse<OrderResponse> response = orderService.createOrderFromCart(orderRequest);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Order created successfully", response.getMessage());

        OrderResponse orderResponse = response.getResult();
        // Tổng tiền trước giảm là 50 * 1 = 50
        assertEquals(new BigDecimal("50"), orderResponse.getTotalPriceBeforeDiscount());
        // Tổng tiền sau giảm do không đủ điều kiện, vẫn bằng 50
        assertEquals(new BigDecimal("50"), orderResponse.getTotalPriceAfterDiscount());

        // Verify order được lưu
        verify(orderRepository, times(1)).save(any(Order.class));
    }


    @Test
    @DisplayName("TC_ORDER_009 - Tổng tiền sau discount âm (cartTotal - discountAmount < 0)")
    void TC_ORDER_009_discountAmountGreaterThanCartTotal_shouldCapDiscountAtCartTotal() {
        // Arrange
        String userId = "user-009";
        String shippingAddressId = "address-009";
        String discountId = "discount-009";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        Cart cart = new Cart();
        cart.setId("cart-009");
        User user = new User();
        user.setId(userId);
        cart.setUser(user);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(shippingAddressId);

        Product product = new Product();
        product.setId("prod-009");
        product.setName("Product 9");
        product.setPrice(new BigDecimal("20"));

        Size size = new Size();
        size.setId("size-009");

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(1);

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(10);

        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setCode("GIAMGIA009");
        discount.setMaxUses(10);
        discount.setUsedCount(0);
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setExpiryDate(LocalDateTime.now().plusDays(1));
        discount.setDiscountType(DiscountType.VALUE);  // Giảm giá cố định
        discount.setDiscountValue(new BigDecimal("50"));  // Giảm tới 50, nhưng giá chỉ 20
        discount.setMaxDiscountValue(new BigDecimal("100"));
        discount.setDiscountPercentage(BigDecimal.ZERO);
        discount.setMinOrderValue(new BigDecimal("10"));  // Điều kiện tổng tiền tối thiểu để được giảm là 10
        discount.setAutoApply(false);

        // Mocks
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("order-009");
            return o;
        });
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productInventoryRepository.findByProductIdAndSizeId(product.getId(), size.getId()))
                .thenReturn(Optional.of(productInventory));
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));
        when(promotionProductRepository.findPromotionApplyByProductId(eq(product.getId()), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            if (inv.getId() == null) {
                inv.setId("invoice-009");
            }
            return inv;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(promotionService.applyPromotionToProduct(any(Product.class)))
                .thenAnswer(invocation -> invocation.<Product>getArgument(0).getPrice());
        when(orderMapper.toOrderResponse(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            OrderResponse response = new OrderResponse();
            response.setTotalPriceBeforeDiscount(order.getTotalPriceBeforeDiscount());
            response.setTotalPriceAfterDiscount(order.getTotalPriceAfterDiscount());
            return response;
        });
        when(userDiscountRepository.findByUserIdAndDiscount_Id(userId, discountId)).thenReturn(Optional.empty());

        // Act
        ApiResponse<OrderResponse> response = orderService.createOrderFromCart(orderRequest);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Order created successfully", response.getMessage());

        OrderResponse orderResponse = response.getResult();
        BigDecimal totalBefore = new BigDecimal("20");
        BigDecimal expectedDiscount = new BigDecimal("20"); // Không thể lớn hơn tổng tiền
        BigDecimal totalAfter = totalBefore.subtract(expectedDiscount);

        assertEquals(totalBefore, orderResponse.getTotalPriceBeforeDiscount());
        assertEquals(totalAfter, orderResponse.getTotalPriceAfterDiscount());

        // Verify lưu order
        verify(orderRepository, times(1)).save(any(Order.class));
    }




    @Test
    @DisplayName("TC_ORDER_010 - Điều kiện hợp lệ, không discount (Cart hợp lệ, không có discountId)")
    void TC_ORDER_010_validCartWithoutDiscount_shouldCreateOrderWithoutDiscount() {
        // Arrange
        String userId = "user-010";
        String shippingAddressId = "address-010";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(null); // Không có discountId

        Cart cart = new Cart();
        cart.setId("cart-010");
        User user = new User();
        user.setId(userId);
        cart.setUser(user);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(shippingAddressId);

        Product product = new Product();
        product.setId("prod-010");
        product.setName("Product 10");
        product.setPrice(new BigDecimal("100"));

        Size size = new Size();
        size.setId("size-010");

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(2);  // 2 sản phẩm

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(10);

        // Mocks
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("order-010");
            return o;
        });
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productInventoryRepository.findByProductIdAndSizeId(product.getId(), size.getId()))
                .thenReturn(Optional.of(productInventory));
        // discountRepository.findById không được gọi vì không có discountId
        when(promotionProductRepository.findPromotionApplyByProductId(eq(product.getId()), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            if (inv.getId() == null) {
                inv.setId("invoice-010");
            }
            return inv;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(promotionService.applyPromotionToProduct(any(Product.class)))
                .thenAnswer(invocation -> invocation.<Product>getArgument(0).getPrice());
        when(orderMapper.toOrderResponse(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            OrderResponse response = new OrderResponse();
            response.setTotalPriceBeforeDiscount(order.getTotalPriceBeforeDiscount());
            response.setTotalPriceAfterDiscount(order.getTotalPriceAfterDiscount());
            return response;
        });

        // userDiscountRepository không gọi vì không có discountId

        // Act
        ApiResponse<OrderResponse> response = orderService.createOrderFromCart(orderRequest);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Order created successfully", response.getMessage());

        OrderResponse orderResponse = response.getResult();
        BigDecimal totalBefore = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity())); // 100 * 2 = 200
        BigDecimal totalAfter = totalBefore; // Không có giảm giá

        assertEquals(totalBefore, orderResponse.getTotalPriceBeforeDiscount());
        assertEquals(totalAfter, orderResponse.getTotalPriceAfterDiscount());

        // Verify order được lưu
        verify(orderRepository, times(1)).save(any(Order.class));
        // discountRepository.findById không được gọi
        verify(discountRepository, never()).findById(any());
        // userDiscountRepository.findByUserIdAndDiscount_Id không được gọi
        verify(userDiscountRepository, never()).findByUserIdAndDiscount_Id(any(), any());
    }




    @Test
    @DisplayName("TC_ORDER_011 - Điều kiện hợp lệ, có discount (Cart hợp lệ + discountId hợp lệ)")
    void TC_ORDER_011_validCartWithValidDiscount_shouldApplyDiscount() {
        // Arrange
        String userId = "user-011";
        String shippingAddressId = "address-011";
        String discountId = "discount-011";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        Cart cart = new Cart();
        cart.setId("cart-011");
        User user = new User();
        user.setId(userId);
        cart.setUser(user);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(shippingAddressId);

        Product product = new Product();
        product.setId("prod-011");
        product.setName("Product 11");
        product.setPrice(new BigDecimal("100"));

        Size size = new Size();
        size.setId("size-011");

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(2);  // 2 sản phẩm

        ProductInventory productInventory = new ProductInventory();
        productInventory.setQuantity(10);

        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setCode("DISCOUNT011");
        discount.setMaxUses(10);
        discount.setUsedCount(0);
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setExpiryDate(LocalDateTime.now().plusDays(1));
        discount.setDiscountType(DiscountType.PERCENTAGE);  // Giảm theo phần trăm
        discount.setDiscountPercentage(new BigDecimal("10"));  // Giảm 10%
        discount.setMaxDiscountValue(new BigDecimal("50"));   // Giảm tối đa 50
        discount.setMinOrderValue(new BigDecimal("100"));     // Tổng đơn phải tối thiểu 100
        discount.setAutoApply(false);

        // Mocks
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("order-011");
            return o;
        });
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(shippingAddress));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productInventoryRepository.findByProductIdAndSizeId(product.getId(), size.getId()))
                .thenReturn(Optional.of(productInventory));
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));
        when(promotionProductRepository.findPromotionApplyByProductId(eq(product.getId()), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            if (inv.getId() == null) {
                inv.setId("invoice-011");
            }
            return inv;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(promotionService.applyPromotionToProduct(any(Product.class)))
                .thenAnswer(invocation -> invocation.<Product>getArgument(0).getPrice());
        when(orderMapper.toOrderResponse(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            OrderResponse response = new OrderResponse();
            response.setTotalPriceBeforeDiscount(order.getTotalPriceBeforeDiscount());
            response.setTotalPriceAfterDiscount(order.getTotalPriceAfterDiscount());
            return response;
        });
        when(userDiscountRepository.findByUserIdAndDiscount_Id(userId, discountId)).thenReturn(Optional.empty());

        // Act
        ApiResponse<OrderResponse> response = orderService.createOrderFromCart(orderRequest);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Order created successfully", response.getMessage());

        OrderResponse orderResponse = response.getResult();
        BigDecimal totalBefore = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity())); // 100 * 2 = 200
        BigDecimal expectedDiscount = totalBefore.multiply(new BigDecimal("10")).divide(new BigDecimal("100")); // 10% của 200 = 20
        BigDecimal totalAfter = totalBefore.subtract(expectedDiscount);

        assertEquals(0, totalBefore.compareTo(orderResponse.getTotalPriceBeforeDiscount()));
        assertEquals(0, totalAfter.compareTo(orderResponse.getTotalPriceAfterDiscount()));


        // Verify lưu order
        verify(orderRepository, times(1)).save(any(Order.class));
        // Verify discountRepository được gọi
        verify(discountRepository, times(2)).findById(discountId);
        // Verify userDiscountRepository được gọi
        verify(userDiscountRepository, times(1)).findByUserIdAndDiscount_Id(userId, discountId);
    }



    @Test
    @DisplayName("TC_ORDER_012 - Đơn hàng không tồn tại (orderId không tồn tại) - Báo lỗi 'Order not found'")
    void TC_ORDER_012_orderIdNotFound_shouldReturnOrderNotFoundError() {
        // Arrange
        String invalidOrderId = "non-existent-order-id";

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        updateRequest.setOrderId(invalidOrderId);
        updateRequest.setShippingAddressId("some-shipping-address-id");

        // Mock order not found
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.clientEditOrder(updateRequest)
        );

        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository, times(1)).findById(invalidOrderId);
        verifyNoMoreInteractions(orderRepository);
    }


    @Test
    @DisplayName("TC_ORDER_013 - User không phải chủ đơn - Báo lỗi 'You do not have permission to edit this order'")
    void TC_ORDER_013_userNotOwner_shouldThrowPermissionError() {
        // Arrange
        String orderId = "order-013";
        String userIdFromRequest = "wrong-user-id"; // ID không khớp với user của đơn hàng

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        updateRequest.setOrderId(orderId);
        updateRequest.setShippingAddressId("some-shipping-id");

        // Mock đơn hàng với user thật
        User actualOwner = new User();
        actualOwner.setId("correct-user-id"); // user thật trong đơn

        Order order = new Order();
        order.setId(orderId);
        order.setUser(actualOwner);
        order.setStatus(OrderStatusEnum.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.clientEditOrder(updateRequest)
        );

        assertEquals("You do not have permission to edit this order", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
    }



    @Test
    @DisplayName("TC_ORDER_014 - Đơn không ở trạng thái Pending - Báo lỗi 'Order cannot be edited at this stage'")
    void TC_ORDER_014_orderNotPending_shouldThrowEditNotAllowedError() {
        // Arrange
        String orderId = "order-014";
        String userId = "user-014";

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        updateRequest.setOrderId(orderId);
        updateRequest.setShippingAddressId("shipping-014");
        updateRequest.setUserId(userId); // Đảm bảo request chứa userId hợp lệ

        // Mock đơn hàng với user đúng nhưng trạng thái không phải PENDING
        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatusEnum.CONFIRMED); // Trạng thái không phải PENDING

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.clientEditOrder(updateRequest)
        );

        assertEquals("Order cannot be edited at this stage", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
    }



    @Test
    @DisplayName("TC_ORDER_015 - Địa chỉ giao hàng mới không tồn tại - Báo lỗi 'Shipping address not found'")
    void TC_ORDER_015_shippingAddressNotFound_shouldThrowException() {
        // Arrange
        String orderId = "order-015";
        String userId = "user-015";
        String nonExistentAddressId = "address-not-found";

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        updateRequest.setOrderId(orderId);
        updateRequest.setUserId(userId);
        updateRequest.setShippingAddressId(nonExistentAddressId);

        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatusEnum.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(shippingAddressRepository.findById(nonExistentAddressId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.clientEditOrder(updateRequest)
        );

        assertEquals("Shipping address not found", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verify(shippingAddressRepository, times(1)).findById(nonExistentAddressId);
        verify(orderRepository, never()).save(any());
    }



    @Test
    @DisplayName("TC_ORDER_016 - Cập nhật địa chỉ giao hàng hợp lệ - Cập nhật thành công")
    void TC_ORDER_016_validShippingAddress_shouldUpdateOrderSuccessfully() {
        // Arrange
        String orderId = "order-016";
        String userId = "user-016";
        String newAddressId = "address-016";

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        updateRequest.setOrderId(orderId);
        updateRequest.setUserId(userId);
        updateRequest.setShippingAddressId(newAddressId);

        User user = new User();
        user.setId(userId);

        ShippingAddress newAddress = new ShippingAddress();
        newAddress.setId(newAddressId);
        newAddress.setAddressDetail("123 New Street");

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatusEnum.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(shippingAddressRepository.findById(newAddressId)).thenReturn(Optional.of(newAddress));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toOrderResponse(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            OrderResponse response = new OrderResponse();
            response.setId(o.getId());
            response.setTotalPriceBeforeDiscount(BigDecimal.ZERO);  // Dummy
            response.setTotalPriceAfterDiscount(BigDecimal.ZERO);   // Dummy
            return response;
        });

        // Act
        ApiResponse<OrderResponse> response = orderService.clientEditOrder(updateRequest);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Order updated successfully", response.getMessage());
        assertEquals(orderId, response.getResult().getId());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(shippingAddressRepository, times(1)).findById(newAddressId);
    }



    @Test
    @DisplayName("TC_ORDER_017 - Đơn hàng không tồn tại - Báo lỗi 'Order not found'")
    void TC_ORDER_017_orderNotFound_shouldThrowException() {
        // Arrange
        String orderId = "invalid-order-017";
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId("any-address");
        orderRequest.setStatus(OrderStatusEnum.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.adminEditOrder(orderId, orderRequest)
        );

        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
    }


    @Test
    @DisplayName("TC_ORDER_018 - Địa chỉ giao hàng mới không tồn tại - Báo lỗi 'Shipping address not found'")
    void TC_ORDER_018_shippingAddressNotFound_shouldThrowException() {
        // Arrange
        String orderId = "order-018";
        String invalidAddressId = "invalid-address-018";

        Order existingOrder = new Order();
        existingOrder.setId(orderId);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId(invalidAddressId);
        orderRequest.setStatus(OrderStatusEnum.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(shippingAddressRepository.findById(invalidAddressId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.adminEditOrder(orderId, orderRequest)
        );

        assertEquals("Shipping address not found", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verify(shippingAddressRepository, times(1)).findById(invalidAddressId);
    }


    @Test
    @DisplayName("TC_ORDER_019 - Cập nhật trạng thái và địa chỉ hợp lệ - Cập nhật thành công")
    void TC_ORDER_019_validStatusAndAddress_shouldUpdateSuccessfully() {
        // Arrange
        String orderId = "order-019";
        String shippingAddressId = "address-019";

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatusEnum.PENDING);

        ShippingAddress newAddress = new ShippingAddress();
        newAddress.setId(shippingAddressId);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setStatus(OrderStatusEnum.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(newAddress));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toOrderResponse(any(Order.class))).thenAnswer(invocation -> {
            Order updatedOrder = invocation.getArgument(0);
            OrderResponse response = new OrderResponse();
            response.setId(updatedOrder.getId());
            response.setStatus(updatedOrder.getStatus());
            return response;
        });

        // Act
        ApiResponse<OrderResponse> response = orderService.adminEditOrder(orderId, orderRequest);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Order updated successfully", response.getMessage());
        assertNotNull(response.getResult());
        assertEquals(OrderStatusEnum.CONFIRMED, response.getResult().getStatus());
        assertEquals(orderId, response.getResult().getId());

        // Verify
        verify(orderRepository, times(1)).findById(orderId);
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderMapper, times(1)).toOrderResponse(any(Order.class));
    }

    @Test
    @DisplayName("TC_ORDER_020 - Lấy danh sách đơn hàng theo userId thành công")
    void TC_GET_ORDER_BY_USER_ID_shouldReturnOrderResponses() {
        // Arrange
        String userId = "user-001";

        List<Order> orders = List.of(
                new Order(), new Order()
        );

        List<OrderResponse> orderResponses = List.of(
                new OrderResponse(), new OrderResponse()
        );

        when(orderRepository.findByUserId(userId)).thenReturn(orders);
        when(orderMapper.toOrderResponseIterable(orders)).thenReturn(orderResponses);

        // Act
        ApiResponse<Iterable<OrderResponse>> response = orderService.getOrderByUserId(userId);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Order retrieved successfully", response.getMessage());
        assertNotNull(response.getResult());
        assertIterableEquals(orderResponses, response.getResult());

        // Verify interactions
        verify(orderRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("TC_ORDER_021 - Lấy trang đơn hàng cho admin thành công")
    void TC_GET_ORDERS_FOR_ADMIN_shouldReturnPagedOrderResponses() {
        // Arrange
        String productName = "ProductA";
        String customerEmail = "customer@example.com";
        OrderStatusEnum status = OrderStatusEnum.PENDING;
        Pageable pageable = PageRequest.of(0, 10);

        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orders = List.of(order1, order2);

        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        OrderResponse response1 = new OrderResponse();
        OrderResponse response2 = new OrderResponse();

        Page<OrderResponse> responsePage = new PageImpl<>(List.of(response1, response2), pageable, orders.size());

        when(orderRepository.findOrdersForAdmin(productName, customerEmail, status, pageable)).thenReturn(orderPage);
        when(orderMapper.toOrderResponse(order1)).thenReturn(response1);
        when(orderMapper.toOrderResponse(order2)).thenReturn(response2);

        // Act
        ApiResponse<Page<OrderResponse>> response = orderService.getOrdersForAdmin(productName, customerEmail, status, pageable);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Orders retrieved successfully", response.getMessage());
        assertNotNull(response.getResult());
        assertEquals(responsePage.getTotalElements(), response.getResult().getTotalElements());
        assertEquals(responsePage.getContent().size(), response.getResult().getContent().size());

        // Verify interactions
        verify(orderRepository, times(1)).findOrdersForAdmin(productName, customerEmail, status, pageable);

    }


}
