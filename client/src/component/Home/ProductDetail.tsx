import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../../redux/store';
import {
    Card,
    Image,
    Typography,
    Divider,
    Button,
    Select,
    InputNumber,
    Row,
    Col,
    Rate,
    Spin,
    Carousel,
    List,
    Avatar
} from 'antd';
import { useAddCartItemMutation } from '../../redux/api/cart';
import { addCartItem } from '../../redux/slice/cartSlice';
import { calculateProductItem } from './services';
import { showCustomNotification } from '../../utils/notification';
import { useGetProductByIdQuery } from '../../redux/api/product-api';
import { useGetProductImageByProductIdQuery } from '../../redux/api/product-image';
import { useGetReviewByProductIdQuery } from '../../redux/api/review';
import { useGetProductInventoryByProductIdQuery } from '../../redux/api/product-inventory';

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;

const ProductDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dispatch = useDispatch();

    const { data: product, isLoading: productLoading } = useGetProductByIdQuery(id ?? "", { skip: !id });
    const { data: productImages, isLoading: productImagesLoading } = useGetProductImageByProductIdQuery(id ?? "", { skip: !id });
    const { data: productReviews, isLoading: productReviewsLoading } = useGetReviewByProductIdQuery(id ?? "", { skip: !id });
    const {
        data: productInventory,
        isLoading: productInventoryLoading
    } = useGetProductInventoryByProductIdQuery(id ?? "", { skip: !id });

    const cartData = useSelector((state: RootState) => state.cart.cart);
    const [addProductToCart, { isLoading: addProductToCartLoading }] = useAddCartItemMutation();

    const [selectedSizeId, setSelectedSizeId] = useState("");
    const [quantity, setQuantity] = useState(1);

    const handleAddProductToCart = async () => {
        const selectedInventory = productInventory?.result?.find((inventory) => inventory.size.id === selectedSizeId);

        if (!selectedInventory) {
            showCustomNotification({
                message: "Please select size",
                type: "error",
            });
            return;
        }

        if (quantity > selectedInventory.quantity) {
            showCustomNotification({
                message: "Quantity exceeds available quantity",
                type: "error",
            });
            return;
        }

        if (!cartData) {
            showCustomNotification({
                message: "Please login to add product to cart",
                type: "error",
            });
            return;
        }

        addProductToCart({
            cartId: cartData?.id ?? "",
            productId: id ?? "",
            sizeId: selectedSizeId,
            quantity: quantity,
        }).unwrap()
            .then((response) => {
                if (response?.result) {
                    dispatch(addCartItem(response.result));
                    showCustomNotification({
                        message: response.message,
                        type: "success",
                    });
                }
            })
            .catch((error) => {
                showCustomNotification({
                    message: error.data.message,
                    type: "error",
                });
            });
    }

    const handleBuyNow = async () => {
        await handleAddProductToCart();
        navigate("/cart");
    }

    if (productLoading || productInventoryLoading || productImagesLoading || productReviewsLoading) {
        return <div className="flex justify-center w-100">
            <Spin size="large" />
        </div>;
    }

    if (!product?.result) return <Text type="danger">Product not found</Text>;
    if (!productInventory?.result) return <Text type="danger">Product inventory not found</Text>;

    const discountPrice = calculateProductItem(product.result);

    return (
        <Row gutter={16} className='p-2'>
            <Col xs={24} sm={24} md={10} lg={10} className='mb-2'>
                <Card>
                    <Carousel
                        dotPosition="bottom"
                        autoplay
                        className='max-height-400 overflow-hidden'
                    >
                        {[product.result.mainImage, ...(productImages?.result || []).map(img => img.imageUrl)].map((imageUrl, index) => (
                            <div key={index} className='height-400 flex justify-center align-center'>
                                <Image
                                    src={imageUrl}
                                    alt={`Product Image ${index + 1}`}
                                    preview
                                    className='max-height-400 object-cover'
                                />
                            </div>
                        ))}
                    </Carousel>
                </Card>
            </Col>

            <Col xs={24} sm={24} md={14} lg={14}>
                <Card>
                    <Title level={3}>{product.result.name}</Title>
                    <div style={{ display: "flex", alignItems: "center", marginBottom: "16px" }}>
                        <Rate
                            disabled
                            allowHalf
                            defaultValue={product?.result.rating || 5}
                            className="fs-16 mr-2"
                        />
                        <Text type="secondary">
                            ({productReviews?.result?.length ?? 0} reviews)
                        </Text>
                    </div>

                    <div className='mb-2'>
                        <Text strong className='fs-24 mr-2 text-secondary'>
                            ₫{discountPrice.toLocaleString()}
                        </Text>
                        <Text delete className='fs-16 text-color-secondary'>
                            ₫{product?.result.price.toLocaleString()}
                        </Text>
                    </div>

                    <Divider />

                    <div className='mb-2'>
                        <Text>Size:</Text>
                        <Select
                            className="w-100"
                            placeholder="Select Size"
                            onChange={(value) => {
                                const selectedSize = productInventory?.result?.find((inventory) => inventory.size.name === value);
                                setSelectedSizeId(selectedSize ? selectedSize.size.id : "");
                            }}
                        >
                            {productInventory.result.map((inventory) => (
                                <Option key={inventory.size.id} value={inventory.size.name}>
                                    {inventory.size.name.toLocaleUpperCase()}
                                </Option>
                            ))}
                        </Select>
                    </div>

                    <div className='mb-2'>
                        <Text>Quantity:</Text>
                        <InputNumber
                            min={1}
                            max={productInventory.result.find((inventory) => inventory.size.id === selectedSizeId)?.quantity || 1}
                            value={quantity}
                            onChange={(value) => setQuantity(value ?? 1)}
                            className='w-100'
                        />
                    </div>

                    {/* Action Buttons */}
                    <div style={{ display: "flex", gap: "10px" }}>
                        <Button
                            type="primary"
                            onClick={handleAddProductToCart}
                            loading={addProductToCartLoading}
                            style={{ flex: 1 }}
                        >
                            Add to Cart
                        </Button>
                        <Button
                            type="default"
                            onClick={handleBuyNow}
                            style={{ flex: 1 }}
                        >
                            Buy Now
                        </Button>
                    </div>
                </Card>
            </Col>

            {/* Reviews Section */}
            <Col span={24} style={{ marginTop: "24px" }}>
                <Card title="Customer Reviews">
                    <List
                        itemLayout="vertical"
                        dataSource={productReviews?.result || []}
                        renderItem={(review) => (
                            <List.Item>
                                <List.Item.Meta
                                    avatar={
                                        <Avatar>
                                            {review.userReviewResponse.name.charAt(0).toUpperCase()}
                                        </Avatar>
                                    }
                                    title={review.userReviewResponse.name}
                                    description={
                                        <Rate
                                            disabled
                                            allowHalf
                                            value={review.rating}
                                        />
                                    }
                                />
                                <Paragraph>{review.content}</Paragraph>
                            </List.Item>
                        )}
                    />
                </Card>
            </Col>
        </Row>
    );
};

export default ProductDetail;