package com.doan.backend.services;

import com.doan.backend.dto.request.OrderRequest;
import com.doan.backend.dto.request.UpdateOrderRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.OrderResponse;
import com.doan.backend.dto.response.ShippingAddressResponse;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.entity.*;
import com.doan.backend.enums.DiscountType;
import com.doan.backend.enums.OrderStatusEnum;
import com.doan.backend.mapper.OrderMapper;
import com.doan.backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

@SpringBootTest
@Transactional
public class TestOrderService {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductInventoryRepository productInventoryRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private DiscountRepository discountRepository;
    @Mock private ShippingAddressRepository shippingAddressRepository;
    @Mock private PromotionProductRepository promotionProductRepository;
    @Mock private PromotionService promotionService;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentService paymentService;
    @Mock private UserDiscountRepository userDiscountRepository;

    private OrderService orderService;
    private String userId;
    private String orderId;
    private Order order;
    private OrderResponse orderResponse;
    private List<Order> orderList;
    private Page<Order> orderPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Khởi tạo OrderService với các mock repository
        orderService = new OrderService(orderRepository, orderItemRepository, cartRepository,
                cartItemRepository, productInventoryRepository, orderMapper, discountRepository,
                shippingAddressRepository, promotionProductRepository, promotionService,
                invoiceRepository, paymentRepository, paymentService, userDiscountRepository);
        MockitoAnnotations.openMocks(this);

        userId = "user123";
        orderId = "order123";

        // Tạo đơn hàng hợp lệ
        order = new Order();
        order.setId(orderId);
        order.setUser(new User());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setTotalPriceBeforeDiscount(new BigDecimal(200));
        order.setTotalPriceAfterDiscount(new BigDecimal(180));

        orderResponse = new OrderResponse();
        orderResponse.setId(orderId);
        orderResponse.setUser(new UserResponse());
        orderResponse.setShippingAddress(new ShippingAddressResponse());
        orderResponse.setOrderItems(new ArrayList<>());
        orderResponse.setTotalPriceBeforeDiscount(new BigDecimal(200));
        orderResponse.setTotalPriceAfterDiscount(new BigDecimal(180));

        orderList = new ArrayList<>();
        orderList.add(order);

        orderPage = new PageImpl<>(orderList);
    }
    @Test
    @DisplayName("TC_ORDER_001 - Cart không tồn tại (cartId không tồn tại trong DB)")
    void testCreateOrderFromCart_CartNotFound() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";

        // Tạo OrderRequest với userId hợp lệ và shippingAddressId hợp lệ
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);

        // Mock cartRepository để trả về Optional.empty(), mô phỏng cart không tồn tại
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        // Kiểm tra thông báo lỗi là "Cart not found"
        assertThat(thrown.getMessage()).isEqualTo("Cart not found");

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("TC_ORDER_002 - Địa chỉ giao hàng không tồn tại (shippingAddressId không tồn tại)")
    void testCreateOrderFromCart_ShippingAddressNotFound() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";

        // Tạo OrderRequest với userId hợp lệ và shippingAddressId hợp lệ
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);

        // Mock cartRepository để trả về Optional.empty(), mô phỏng cart không tồn tại
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart()));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        // Kiểm tra thông báo lỗi là "Shipping address not found"
        assertThat(thrown.getMessage()).isEqualTo("Shipping address not found");

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
    }


    @Test
    @DisplayName("TC_ORDER_003 - Product inventory không tồn tại (productInventoryId không tồn tại)")
    void testCreateOrderFromCart_ProductInventoryNotFound() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";
        String productId = "product123";
        String sizeId = "size123";

        // Tạo OrderRequest với userId hợp lệ và shippingAddressId hợp lệ
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);

        // Tạo Cart giả và CartItem
        Cart cart = new Cart();
        cart.setUser(new User()); // Giả sử User có sẵn trong hệ thống, hoặc mock nếu cần
        cart.setId("cart123"); // ID giỏ hàng giả định

        Product product = new Product();
        product.setId(productId);  // ID sản phẩm giả định
        Size size = new Size();
        size.setId(sizeId);        // ID kích thước giả định

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(2); // Giả sử giỏ hàng có 2 sản phẩm
        cartItem.setCart(cart); // Liên kết CartItem với Cart

        // Mock cartRepository để trả về cart tồn tại
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Mock shippingAddressRepository trả về địa chỉ tồn tại
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));

        // Mock productInventoryRepository trả về Optional.empty(), mô phỏng productInventory không tồn tại
        when(productInventoryRepository.findByProductIdAndSizeId(productId, sizeId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        // Kiểm tra thông báo lỗi là "Product inventory not found"
        assertThat(thrown.getMessage()).isEqualTo("Product inventory not found");

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(productInventoryRepository, times(1)).findByProductIdAndSizeId(productId, sizeId);
    }



    @Test
    @DisplayName("TC_ORDER_004 - Không đủ tồn kho (quantity > availableStock)")
    void testCreateOrderFromCart_InsufficientStock() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";
        String productId = "product123";
        String sizeId = "size123";  // Thêm kích thước sản phẩm
        int requestedQuantity = 10;  // Số lượng yêu cầu từ giỏ hàng
        int availableStock = 5;      // Số lượng tồn kho thực tế

        // Tạo OrderRequest với userId hợp lệ và shippingAddressId hợp lệ
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);

        // Tạo Cart giả và CartItem
        Cart cart = new Cart();
        cart.setUser(new User());  // Giả sử User có sẵn trong hệ thống, hoặc mock nếu cần
        cart.setId("cart123");     // ID giỏ hàng giả định

        // Tạo Product và Size
        Product product = new Product();
        product.setId(productId);  // ID sản phẩm giả định

        Size size = new Size();
        size.setId(sizeId);        // ID kích thước giả định

        // Tạo CartItem giả với số lượng yêu cầu
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(requestedQuantity);  // Số lượng yêu cầu trong giỏ hàng
        cartItem.setCart(cart);  // Liên kết CartItem với Cart

        // Mock cartRepository để trả về cart tồn tại
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Mock shippingAddressRepository trả về địa chỉ tồn tại
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));

        // Mock productInventoryRepository trả về số lượng tồn kho không đủ cho sản phẩm với kích thước cụ thể
        ProductInventory productInventory = new ProductInventory();
        productInventory.setProduct(product);    // Liên kết với sản phẩm
        productInventory.setSize(size);          // Liên kết với kích thước
        productInventory.setQuantity(availableStock);  // Số lượng tồn kho ít hơn số lượng yêu cầu
        when(productInventoryRepository.findByProductIdAndSizeId(productId, sizeId)).thenReturn(Optional.of(productInventory));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        // Kiểm tra thông báo lỗi là "Insufficient stock for product"
        assertThat(thrown.getMessage()).isEqualTo("Insufficient stock for product");

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(productInventoryRepository, times(1)).findByProductIdAndSizeId(productId, sizeId);
    }
    @Test
    @DisplayName("TC_ORDER_005 - Mã giảm giá đã được sử dụng (discountId đã được used)")
    void testCreateOrderFromCart_DiscountUsed() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";
        String discountId = "discount123";  // ID mã giảm giá giả định

        // Tạo OrderRequest với userId hợp lệ và shippingAddressId hợp lệ
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);  // Thêm discountId vào request

        // Mock cartRepository để trả về cart tồn tại
        Cart cart = new Cart();
        cart.setUser(new User());
        cart.setId("cart123");
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Mock shippingAddressRepository trả về địa chỉ tồn tại
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));

        // Mock DiscountRepository để trả về discount đã được sử dụng
        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setUsedCount(1);  // Đánh dấu discount đã được sử dụng (usedCount > 0)
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        // Kiểm tra thông báo lỗi là "Discount has been used"
        assertThat(thrown.getMessage()).isEqualTo("Discount has been used");

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(discountRepository, times(1)).findById(discountId);
    }


    @Test
    @DisplayName("TC_ORDER_006 - Mã giảm giá hết lượt sử dụng (discount.quantity == 0)")
    void testCreateOrderFromCart_DiscountOutOfStock() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";
        String discountId = "discount123";  // ID mã giảm giá giả định

        // Tạo OrderRequest với userId hợp lệ và shippingAddressId hợp lệ
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);  // Thêm discountId vào request

        // Mock cartRepository để trả về cart tồn tại
        Cart cart = new Cart();
        cart.setUser(new User());
        cart.setId("cart123");
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Mock shippingAddressRepository trả về địa chỉ tồn tại
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));

        // Mock DiscountRepository để trả về discount hết lượt sử dụng (quantity == 0)
        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setUsedCount(0);  // Cái này không quan trọng trong trường hợp này
        discount.setMaxUses(0);  // Mã giảm giá đã hết lượt sử dụng
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        // Kiểm tra thông báo lỗi là "Discount are out of stock"
        assertThat(thrown.getMessage()).isEqualTo("Discount are out of stock");

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(discountRepository, times(1)).findById(discountId);
    }


    @Test
    @DisplayName("TC_ORDER_007 - Mã giảm giá chưa tới hạn hoặc đã hết hạn (currentDate not in [startDate, endDate])")
    void testCreateOrderFromCart_DiscountNotValid() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";
        String discountId = "discount123";  // ID mã giảm giá giả định

        // Tạo OrderRequest với userId hợp lệ và shippingAddressId hợp lệ
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);  // Thêm discountId vào request

        // Mock cartRepository để trả về cart tồn tại
        Cart cart = new Cart();
        cart.setUser(new User());
        cart.setId("cart123");
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Mock shippingAddressRepository trả về địa chỉ tồn tại
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));

        // Mock DiscountRepository để trả về discount chưa tới hạn hoặc đã hết hạn
        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setStartDate(LocalDateTime.now().plusDays(1));  // Mã giảm giá chưa tới hạn
        discount.setExpiryDate(LocalDateTime.now().minusDays(1)); // Mã giảm giá đã hết hạn
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(orderRequest);
        });

        // Kiểm tra thông báo lỗi là "Discount is not yet valid"
        assertThat(thrown.getMessage()).isEqualTo("Discount is not yet valid");

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(discountRepository, times(1)).findById(discountId);
    }

    @Test
    @DisplayName("TC_ORDER_008 - Tổng tiền không đạt điều kiện discount (cartTotal < discount.minOrderAmount)")
    void testCreateOrderFromCart_CartTotalNotEnoughForDiscount() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";
        String discountId = "discount123";

        // Tạo OrderRequest
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        // Giả lập tổng tiền giỏ hàng nhỏ hơn discount.minOrderValue
        BigDecimal productPrice = BigDecimal.valueOf(30.00);
        int requestedQuantity = 2;
        BigDecimal cartTotal = productPrice.multiply(BigDecimal.valueOf(requestedQuantity));
        BigDecimal minOrderValue = BigDecimal.valueOf(100.00); // Discount yêu cầu 100

        // Mock Cart
        Cart cart = new Cart();
        cart.setId("cart123");
        cart.setUser(new User());

        // Mock CartItem
        Product product = new Product();
        product.setPrice(productPrice);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(requestedQuantity);

        // Mock CartRepository
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // Mock CartItemRepository
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));

        // Mock ShippingAddressRepository
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));

        // Mock DiscountRepository
        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setMinOrderValue(minOrderValue); // minOrderValue = 100
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));

        // Act
        ApiResponse<OrderResponse> response = orderService.createOrderFromCart(orderRequest);
        OrderResponse orderResponse = response.getResult();

        // Assert
        assertThat(orderResponse.getTotalPriceBeforeDiscount()).isEqualTo(cartTotal);
        assertThat(orderResponse.getTotalPriceAfterDiscount()).isEqualTo(cartTotal);  // Không giảm giá

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartItemRepository, times(1)).findByCartId(cart.getId());
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(discountRepository, times(1)).findById(discountId);
    }


    @Test
    @DisplayName("TC_ORDER_009 - Tổng tiền sau discount âm (cartTotal - discountAmount < 0)")
    void testCreateOrderFromCart_TotalAfterDiscountNegative() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";
        String discountId = "discount123";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        BigDecimal productPrice = BigDecimal.valueOf(20.00);
        int requestedQuantity = 1;
        BigDecimal cartTotal = productPrice.multiply(BigDecimal.valueOf(requestedQuantity)); // cartTotal = 20

        // Cart
        Cart cart = new Cart();
        cart.setId("cart123");
        cart.setUser(new User());

        Product product = new Product();
        product.setPrice(productPrice);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(requestedQuantity);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));

        // Discount
        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setDiscountType(DiscountType.VALUE); // Dùng VALUE (giảm theo số tiền cố định)
        discount.setDiscountValue(BigDecimal.valueOf(50.00)); // Giảm 50 > cartTotal 20
        discount.setMaxDiscountValue(BigDecimal.valueOf(50.00));
        discount.setMinOrderValue(BigDecimal.ZERO);
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));

        // Act
        ApiResponse<OrderResponse> response = orderService.createOrderFromCart(orderRequest);
        OrderResponse orderResponse = response.getResult();

        // Assert
        assertThat(orderResponse.getTotalPriceBeforeDiscount()).isEqualTo(cartTotal);
        assertThat(orderResponse.getTotalPriceAfterDiscount()).isEqualTo(BigDecimal.ZERO);

        // Verify
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartItemRepository, times(1)).findByCartId(cart.getId());
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(discountRepository, times(1)).findById(discountId);
    }


    @Test
    @DisplayName("TC_ORDER_010 - Điều kiện hợp lệ, không discount (Cart hợp lệ, không có discountId)")
    void testCreateOrderFromCart_NoDiscountApplied() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";

        // Không set discountId
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);

        // Setup cart + cartItems
        BigDecimal productPrice = BigDecimal.valueOf(50.00);
        int requestedQuantity = 2;
        BigDecimal cartTotal = productPrice.multiply(BigDecimal.valueOf(requestedQuantity)); // 100

        Cart cart = new Cart();
        cart.setId("cart123");
        cart.setUser(new User());

        Product product = new Product();
        product.setPrice(productPrice);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(requestedQuantity);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));

        // Act
        ApiResponse<OrderResponse> response = orderService.createOrderFromCart(orderRequest);
        OrderResponse orderResponse = response.getResult();

        // Assert
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getTotalPriceBeforeDiscount()).isEqualTo(cartTotal);
        assertThat(orderResponse.getTotalPriceAfterDiscount()).isEqualTo(cartTotal); // Không discount nên 2 số bằng nhau

        // Verify repository interactions
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartItemRepository, times(1)).findByCartId(cart.getId());
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verifyNoInteractions(discountRepository); // Không gọi đến discountRepository
    }

    @Test
    @DisplayName("TC_ORDER_011 - Điều kiện hợp lệ, có discount (Cart hợp lệ + discountId hợp lệ)")
    void testCreateOrderFromCart_ValidCartAndDiscountApplied() {
        // Arrange
        String userId = "user123";
        String shippingAddressId = "address123";
        String discountId = "discount123";

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setDiscountId(discountId);

        // Setup cart + cartItems
        BigDecimal productPrice = BigDecimal.valueOf(100.00);
        int requestedQuantity = 1;
        BigDecimal cartTotal = productPrice.multiply(BigDecimal.valueOf(requestedQuantity)); // 100

        Cart cart = new Cart();
        cart.setId("cart123");
        cart.setUser(new User());

        Product product = new Product();
        product.setPrice(productPrice);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(requestedQuantity);

        // Discount setup
        Discount discount = new Discount();
        discount.setId(discountId);
        discount.setDiscountType(DiscountType.PERCENTAGE);
        discount.setDiscountPercentage(BigDecimal.valueOf(10)); // 10% giảm giá
        discount.setMaxDiscountValue(BigDecimal.valueOf(50));   // Tối đa được giảm 50
        discount.setMinOrderValue(BigDecimal.valueOf(50));      // Hóa đơn tối thiểu 50 mới áp dụng
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setExpiryDate(LocalDateTime.now().plusDays(5));
        discount.setUsedCount(0);
        discount.setMaxUses(100);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(shippingAddressRepository.findById(shippingAddressId)).thenReturn(Optional.of(new ShippingAddress()));
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));

        // Act
        ApiResponse<OrderResponse> response = orderService.createOrderFromCart(orderRequest);
        OrderResponse orderResponse = response.getResult();

        // Calculate expected
        BigDecimal expectedDiscountAmount = cartTotal.multiply(discount.getDiscountPercentage().divide(BigDecimal.valueOf(100)));
        if (expectedDiscountAmount.compareTo(discount.getMaxDiscountValue()) > 0) {
            expectedDiscountAmount = discount.getMaxDiscountValue();
        }
        BigDecimal expectedTotalAfterDiscount = cartTotal.subtract(expectedDiscountAmount);

        // Assert
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getTotalPriceBeforeDiscount()).isEqualTo(cartTotal);
        assertThat(orderResponse.getTotalPriceAfterDiscount()).isEqualTo(expectedTotalAfterDiscount);

        // Verify behavior
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartItemRepository, times(1)).findByCartId(cart.getId());
        verify(shippingAddressRepository, times(1)).findById(shippingAddressId);
        verify(discountRepository, times(1)).findById(discountId);
    }



    @Test
    @DisplayName("TC_ORDER_012 - Đơn hàng không tồn tại (orderId không tồn tại) - Báo lỗi 'Order not found'")
    void testClientEditOrder_OrderNotFound() {
        // Arrange
        String invalidOrderId = "invalid-order-id";
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setOrderId(invalidOrderId);
        request.setUserId("user123"); // userId hợp lệ, nhưng orderId sai

        // Mock orderRepository không tìm thấy Order
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.clientEditOrder(request);
        });

        // Verify exception message
        assertThat(exception.getMessage()).isEqualTo("Order not found");

        // Verify repository call
        verify(orderRepository, times(1)).findById(invalidOrderId);
    }

    @Test
    @DisplayName("TC_ORDER_013 - User không phải chủ đơn - Báo lỗi 'You do not have permission to edit this order'")
    void testClientEditOrder_UserNotOwner() {
        // Arrange
        String orderId = "order123";
        String userIdInRequest = "user123";    // user gửi request
        String ownerIdInOrder = "differentUser"; // user thật sự của đơn

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setOrderId(orderId);
        request.setUserId(userIdInRequest);

        // Tạo Order có user khác với user gửi request
        User ownerUser = new User();
        ownerUser.setId(ownerIdInOrder);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(ownerUser);

        // Mock orderRepository trả về order
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.clientEditOrder(request);
        });

        assertThat(exception.getMessage()).isEqualTo("You do not have permission to edit this order");

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("TC_ORDER_014 - Đơn không ở trạng thái Pending - Báo lỗi 'Order cannot be edited at this stage'")
    void testClientEditOrder_OrderNotPending() {
        // Arrange
        String orderId = "order123";
        String userId = "user123";

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);

        // Tạo Order đúng user nhưng status khác Pending
        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatusEnum.CONFIRMED); // Không phải PENDING

        // Mock orderRepository trả về order
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.clientEditOrder(request);
        });

        assertThat(exception.getMessage()).isEqualTo("Order cannot be edited at this stage");

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("TC_ORDER_015 - Địa chỉ giao hàng mới không tồn tại - Báo lỗi 'Shipping address not found'")
    void testClientEditOrder_ShippingAddressNotFound() {
        // Arrange
        String orderId = "order123";
        String userId = "user123";
        String invalidShippingAddressId = "invalidAddressId"; // Địa chỉ không tồn tại

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setShippingAddressId(invalidShippingAddressId);  // Địa chỉ mới không hợp lệ

        // Tạo User và Order hợp lệ
        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatusEnum.PENDING);  // Trạng thái đơn hợp lệ để chỉnh sửa

        // Mock orderRepository trả về order
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock shippingAddressRepository trả về null khi tìm kiếm địa chỉ
        when(shippingAddressRepository.findById(invalidShippingAddressId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.clientEditOrder(request);
        });

        assertThat(exception.getMessage()).isEqualTo("Shipping address not found");

        verify(orderRepository, times(1)).findById(orderId);
        verify(shippingAddressRepository, times(1)).findById(invalidShippingAddressId);
    }

    @Test
    @DisplayName("TC_ORDER_016 - Cập nhật địa chỉ giao hàng hợp lệ - Cập nhật thành công")
    void testClientEditOrder_ShippingAddressUpdateSuccess() {
        // Arrange
        String orderId = "order123";
        String userId = "user123";
        String validShippingAddressId = "validAddressId";  // Địa chỉ mới hợp lệ

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setShippingAddressId(validShippingAddressId);  // Địa chỉ mới hợp lệ

        // Tạo User và Order hợp lệ
        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatusEnum.PENDING);  // Trạng thái đơn hợp lệ để chỉnh sửa

        // Tạo ShippingAddress hợp lệ
        ShippingAddress validShippingAddress = new ShippingAddress();
        validShippingAddress.setId(validShippingAddressId);

        // Mock orderRepository trả về order
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock shippingAddressRepository trả về địa chỉ hợp lệ
        when(shippingAddressRepository.findById(validShippingAddressId)).thenReturn(Optional.of(validShippingAddress));

        // Act
        ApiResponse<OrderResponse> response = orderService.clientEditOrder(request);

        // Assert
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Order updated successfully");

        // Verify that the order's shipping address was updated
        assertThat(order.getShippingAddress()).isEqualTo(validShippingAddress);

        verify(orderRepository, times(1)).findById(orderId);
        verify(shippingAddressRepository, times(1)).findById(validShippingAddressId);
    }


    @Test
    @DisplayName("TC_ORDER_017 - Đơn hàng không tồn tại - Báo lỗi 'Order not found'")
    void testAdminEditOrder_OrderNotFound() {
        // Arrange
        String invalidOrderId = "invalidOrderId";
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId("validAddressId");

        // Mock orderRepository trả về Optional.empty() khi tìm kiếm đơn hàng không tồn tại
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.adminEditOrder(invalidOrderId, orderRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("Order not found");

        verify(orderRepository, times(1)).findById(invalidOrderId);
    }

    @Test
    @DisplayName("TC_ORDER_018 - Địa chỉ giao hàng mới không tồn tại - Báo lỗi 'Shipping address not found'")
    void testAdminEditOrder_ShippingAddressNotFound() {
        // Arrange
        String orderId = "order123";
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId("invalidAddressId"); // Địa chỉ không tồn tại

        // Tạo đơn hàng hợp lệ
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatusEnum.PENDING);  // Trạng thái đơn hợp lệ để chỉnh sửa

        // Mock orderRepository trả về order
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock shippingAddressRepository trả về null khi tìm kiếm địa chỉ
        when(shippingAddressRepository.findById("invalidAddressId")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.adminEditOrder(orderId, orderRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("Shipping address not found");

        verify(orderRepository, times(1)).findById(orderId);
        verify(shippingAddressRepository, times(1)).findById("invalidAddressId");
    }
    @Test
    @DisplayName("TC_ORDER_019 - Cập nhật trạng thái và địa chỉ hợp lệ - Cập nhật thành công")
    void testAdminEditOrder_UpdateStatusAndShippingAddressSuccess() {
        // Arrange
        String orderId = "order123";
        String newShippingAddressId = "newAddressId";
        OrderStatusEnum newStatus = OrderStatusEnum.SHIPPING;

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId(newShippingAddressId);
        orderRequest.setStatus(newStatus);

        // Tạo đơn hàng hợp lệ
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatusEnum.PENDING);  // Trạng thái đơn hợp lệ để chỉnh sửa

        // Tạo ShippingAddress hợp lệ
        ShippingAddress validShippingAddress = new ShippingAddress();
        validShippingAddress.setId(newShippingAddressId);

        // Mock orderRepository trả về order
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock shippingAddressRepository trả về địa chỉ hợp lệ
        when(shippingAddressRepository.findById(newShippingAddressId)).thenReturn(Optional.of(validShippingAddress));

        // Act
        ApiResponse<OrderResponse> response = orderService.adminEditOrder(orderId, orderRequest);

        // Assert
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Order updated successfully");

        // Verify that the order's shipping address and status were updated
        assertThat(order.getShippingAddress()).isEqualTo(validShippingAddress);
        assertThat(order.getStatus()).isEqualTo(newStatus);

        verify(orderRepository, times(1)).findById(orderId);
        verify(shippingAddressRepository, times(1)).findById(newShippingAddressId);
    }


    @Test
    @DisplayName("TC_ORDER_020")
    void GetOrderByUserId_OrderNotFound() {
        // Arrange: Khi không có đơn hàng cho userId
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act: Gọi phương thức getOrderByUserId
        ApiResponse<Iterable<OrderResponse>> response = orderService.getOrderByUserId(userId);

        // Assert: Kiểm tra thông báo lỗi "Order not found"
        assertEquals(200, response.getCode());
        assertEquals("Order retrieved successfully", response.getMessage());
        assertTrue(((List<OrderResponse>) response.getResult()).isEmpty());
    }

    // Kiểm tra trường hợp đơn hàng hợp lệ cho userId
    @Test
    @DisplayName("TC_ORDER_021")
    void GetOrderByUserId_ValidOrder() {
        // Arrange: Khi có đơn hàng cho userId
        when(orderRepository.findByUserId(userId)).thenReturn(orderList);
        when(orderMapper.toOrderResponseIterable(orderList)).thenReturn(Collections.singletonList(orderResponse));

        // Act: Gọi phương thức getOrderByUserId
        ApiResponse<Iterable<OrderResponse>> response = orderService.getOrderByUserId(userId);

        // Assert: Kiểm tra thông tin đơn hàng trả về
        assertEquals(200, response.getCode());
        assertEquals("Order retrieved successfully", response.getMessage());
        assertEquals(1, ((List<OrderResponse>) response.getResult()).size());
        assertEquals(orderId, ((List<OrderResponse>) response.getResult()).get(0).getId());
    }

    // Kiểm tra lấy danh sách đơn hàng cho admin
    @Test
    @DisplayName("TC_ORDER_022")
    void GetOrdersForAdmin_ValidOrders() {
        // Arrange: Khi có danh sách đơn hàng cho các bộ lọc
        Pageable pageable = mock(Pageable.class);
        when(orderRepository.findOrdersForAdmin(anyString(), anyString(), any(), eq(pageable)))
                .thenReturn(orderPage);
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        // Act: Gọi phương thức getOrdersForAdmin
        ApiResponse<Page<OrderResponse>> response = orderService.getOrdersForAdmin(
                "Product1", "customer@example.com", OrderStatusEnum.PENDING, pageable);

        // Assert: Kiểm tra kết quả trả về
        assertEquals(200, response.getCode());
        assertEquals("Orders retrieved successfully", response.getMessage());
        assertEquals(1, response.getResult().getContent().size());
        assertEquals(orderId, response.getResult().getContent().get(0).getId());
    }


}
