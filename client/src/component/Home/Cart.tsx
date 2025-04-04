import {useDispatch, useSelector} from "react-redux";
import {RootState} from "../../redux/store";
import {Button, Col, Divider, Image, Input, Row, Typography} from 'antd';
import {DeleteOutlined, MinusOutlined, PlusOutlined} from '@ant-design/icons';
import {CartItemResponse} from "../../types/cart";
import '../../sass/cart.scss';
import {useDeleteCartItemMutation, useUpdateCartItemMutation} from "../../redux/api/cart";
import {showCustomNotification} from "../../utils/notification";
import {deleteCartItem, updateCartItem} from "../../redux/slice/cartSlice";
import {useNavigate} from "react-router-dom";
import { calculateItemTotal, calculateSubtotal } from "./services";
import ConfirmModal from "../../utils/ConfirmModal";
import { useState } from "react";

const {Title, Text} = Typography;

const Cart = () => {
    const navigate = useNavigate()
    const cartData = useSelector((state: RootState) => state.cart.cart);
    const dispatch = useDispatch();
    const [triggerUpdateCartItem, {isLoading: isUpdatingCartItem}] = useUpdateCartItemMutation();
    const [triggerDeleteCartItem, {isLoading: isDeleting}] = useDeleteCartItemMutation();
    const [showConfirmDeleteModal, setShowConfirmDeleteModal] = useState<{
        isOpen: boolean;
        cartItemId: string;
    }>({
        isOpen: false,
        cartItemId: ""
    });

    const handleDeleteCartItem = (cartItemId: string) => {
        triggerDeleteCartItem({
            id: cartItemId,
            cartId: cartData?.id ?? ""
        }).unwrap()
            .then((response) => {
                dispatch(deleteCartItem(cartItemId));
                showCustomNotification({
                    message: response.message,
                    type: "success"
                });
                setShowConfirmDeleteModal({isOpen: false, cartItemId: ""});
            }).catch((error) => {
            showCustomNotification({
                message: error.data.message,
                type: "error"
            });
        });
    };

    const handleUpdateCartItem = (cartItem: CartItemResponse, quantity: number) => {
        triggerUpdateCartItem({
            id: cartItem.id,
            cartItemRequest: {
                cartId: cartData?.id ?? "",
                productId: cartItem.product.id,
                sizeId: cartItem.size.id,
                quantity
            }
        })
            .unwrap()
            .then((response) => {
                if (response?.result) {
                    dispatch(updateCartItem({
                        ...cartItem,
                        quantity
                    }));
                    showCustomNotification({
                        message: response.message,
                        type: "success"
                    });
                }
            })
            .catch((error) => {
                showCustomNotification({
                    message: error.data.message,
                    type: "error"
                });
            });
    };

    return (
        <Row gutter={24} className="cart-container" justify="center">
            <Col xs={24} md={16} className="cart-items">
                <Title level={3}>Shopping Cart</Title>
                {cartData?.cartItems.map((item) => (
                    <div key={item.id} className="cart-item">
                        <Image src={item.product.mainImage} alt={item.product.name} width={80}/>
                        <div className="product-details">
                            <Title level={5}>{item.product.name}/{item.size.name}</Title>
                            <Text className="product-size">Size: {item.size.name}</Text>
                            <div className="product-price">
                                <Text
                                    className="discounted-price">{item.product.price * (1 - item.product.discountPercentage / 100)}₫</Text>
                                <Text delete className="original-price">{item.product.price}₫</Text>
                            </div>
                            <div className="product-quantity">
                                <Button 
                                    onClick={() => {
                                        void handleUpdateCartItem(item, item.quantity - 1)
                                    }}  
                                    icon={<MinusOutlined/>} size="small"
                                    loading={isUpdatingCartItem}
                                />
                                <Text>{item.quantity}</Text>
                                <Button onClick={() => handleUpdateCartItem(item, item.quantity + 1)}
                                        icon={<PlusOutlined/>} size="small"/>
                            </div>
                        </div>
                        <div className="product-total">
                            <Text strong>{calculateItemTotal(item)}₫</Text>
                            <Button type="text" onClick={() => setShowConfirmDeleteModal({
                                isOpen: true,
                                cartItemId: item.id
                            })} danger
                                    icon={<DeleteOutlined/>}/>
                        </div>
                    </div>
                ))}
                <Button type="primary" danger={true} onClick={() => navigate("/")}>CONTINUE SHOPPING</Button>
                <Divider/>
                <Title level={5}>Order Note</Title>
                <Input.TextArea placeholder="Notes" rows={4}/>
            </Col>
            <Col xs={24} md={8} className="cart-summary">
                <Title level={3}>Order Summary</Title>
                <div className="final-total">
                    <Text strong>Total: {" "}</Text>
                    <Text strong className="total-amount">{calculateSubtotal(cartData)}₫</Text>
                </div>
                <Button type="primary" danger={true} block onClick={() => navigate("/checkout")}>CHECKOUT</Button>
            </Col>
            <ConfirmModal 
                title="Delete Cart Item"
                message="Are you sure you want to delete this item?"
                onConfirm={() => handleDeleteCartItem(showConfirmDeleteModal.cartItemId)}
                onClose={() => setShowConfirmDeleteModal({isOpen: false, cartItemId: ""})}
                isOpen={showConfirmDeleteModal.isOpen}
                isSubmitLoading={isDeleting}
            />
        </Row>
    );
};

export default Cart;