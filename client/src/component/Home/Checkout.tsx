import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { RootState } from "../../redux/store";
import { Field, FieldProps, Form as FormikForm, Formik, FormikProps } from 'formik';
import { useCreateOrderFromCartMutation } from "../../redux/api/order";
import { useEffect, useState } from "react";
import * as Yup from 'yup';
import { Button, Col, Divider, Form, Image, Row, Select, Spin, Typography } from "antd";
import { DiscountResponse } from "../../types/discount";
import DiscountModal from "./DiscountModal";
import { CartItemResponse } from "../../types/cart";
import CreateOrUpdateShippingAddressModal from "../Auth/CreateOrUpdateShippingAddressModal";
import { useGetShippingAddressByUserIdQuery } from "../../redux/api/shipping-address";
import { PlusOutlined } from "@ant-design/icons";
import { ReactComponent as VoucherIcon } from "../../img/svg/voucher_icon.svg";
import "../../sass/checkout.scss";
import { showCustomNotification } from "../../utils/notification";
import { useDispatch } from "react-redux";
import { clearCart } from "../../redux/slice/cartSlice";
import { GHTKFee, GHTKFeeRequest, ShippingAddressRequest } from "../../types/shipping-address";
import { baseApi } from "../../redux/api/auth-api";
import axios, { AxiosResponse } from "axios";
import { BaseResponse } from "../../types/base-response";

const { Title, Text } = Typography;

const CheckoutSchema = Yup.object().shape({
    shippingAddressId: Yup.string().required("Please select a shipping address"),
    discountId: Yup.string(),
});

interface CreateOrderFromCartRequest {
    shippingAddressId: string;
    discountId: string | null;
}

const getGHTKShippingFee = async (gHTKFeeRequest: GHTKFeeRequest) => {
    try {
        const url = `${baseApi}/ghtk/shipping-costs`;

        const response = await axios.post(url, {
            addressDetail: gHTKFeeRequest.addressDetail,
            city: gHTKFeeRequest.city,
            district: gHTKFeeRequest.district,
            ward: gHTKFeeRequest.ward,
            value: gHTKFeeRequest.value,
        });

        const res = response as AxiosResponse<BaseResponse<GHTKFee>>
        return res.data.result?.fee;
    } catch (error) {
        console.error("Error fetching GHTK shipping fee:", error);
    }
};

const Checkout = () => {
    const navigate = useNavigate();
    const cartData = useSelector((state: RootState) => state.cart.cart);
    const userInfo = useSelector((state: RootState) => state.user.user);
    const dispatch = useDispatch();
    const { data: shippingAddressData, refetch, isLoading } = useGetShippingAddressByUserIdQuery(userInfo?.id ?? "");
    const [createOrderFromCart, { isLoading: createOrderFromCartLoading }] = useCreateOrderFromCartMutation();
    const [selectedDiscount, setSelectedDiscount] =
        useState<DiscountResponse>();
    const [shippingFee, setShippingFee] = useState<number>(35000);

    const [showDiscountModal, setShowDiscountModal] = useState(false);
    const [showShippingAddressModal, setShowShippingAddressModal] =
        useState(false);

    const [initialValues, setInitialValues] = useState<CreateOrderFromCartRequest>({
        shippingAddressId: "",
        discountId: "",
    });

    const fetchShippingFee = async (addressId: string) => {
        const selectedAddress = shippingAddressData?.result?.find(
            (address) => address.id === addressId
        );
        if (selectedAddress) {
            const fee = await getGHTKShippingFee({
                addressDetail: selectedAddress.addressDetail,
                city: selectedAddress.city,
                district: selectedAddress.district,
                ward: selectedAddress.ward,
                value: calculateSubtotal(),
            });
            setShippingFee(35000);
        }
    };

    const handleAddressChange = (addressId: string, form: FormikProps<any>) => {
        form.setFieldValue("shippingAddressId", addressId);
        fetchShippingFee(addressId);
    };

    useEffect(() => {
        if (initialValues.shippingAddressId) {
            fetchShippingFee(initialValues.shippingAddressId);
        }
    }, [initialValues.shippingAddressId]);

    useEffect(() => {
        if (shippingAddressData?.result) {
            setInitialValues({
                shippingAddressId: shippingAddressData.result.find(address => address.isDefault)?.id ?? "",
                discountId: "",
            });
        }
    }, [shippingAddressData]);

    const handleSubmit = async (values: CreateOrderFromCartRequest) => {
        createOrderFromCart({
            userId: userInfo?.id ?? "",
            discountId: selectedDiscount?.id,
            shippingAddressId: values.shippingAddressId,
        })
            .unwrap()
            .then((response) => {
                dispatch(clearCart());
                showCustomNotification({
                    message: response.message,
                    type: "success"
                });
                navigate("/account");
            })
            .catch((error) => {
                showCustomNotification({
                    message: error.data.message,
                    type: "error"
                });
            });
    };

    const calculateItemTotal = (item: CartItemResponse) => {
        const discountedPrice = item.product.price * (1 - item.product.discountPercentage / 100);
        return discountedPrice * item.quantity;
    };

    const calculateSubtotal = () => {
        return cartData?.cartItems.reduce((total, item) => total + calculateItemTotal(item), 0) || 0;
    };

    const calculateDiscountAmount = () => {
        if (!selectedDiscount) return 0;

        const subtotal = calculateSubtotal();

        if (selectedDiscount.discountType === "PERCENTAGE") {
            const percentageDiscount = (subtotal * (selectedDiscount.discountPercentage ?? 0)) / 100;
            return Math.min(percentageDiscount, selectedDiscount.maxDiscountValue);
        }

        if (selectedDiscount.discountType === "VALUE") {
            return Math.min(selectedDiscount.discountValue ?? 0, selectedDiscount.maxDiscountValue);
        }

        return 0;
    };

    const calculateTotal = () => {
        const subtotal = calculateSubtotal();
        const discount = calculateDiscountAmount();
        return subtotal - discount + shippingFee;
    };

    return (
        <div className="checkout-container">
            {(isLoading || createOrderFromCartLoading) ? <div className="flex justify-center w-100">
                <Spin size="large" />
            </div> : (
                <Formik
                    initialValues={initialValues}
                    validationSchema={CheckoutSchema}
                    onSubmit={handleSubmit}
                    enableReinitialize
                >
                    {({ errors, touched }) => (
                        <FormikForm>
                            <Row gutter={24}>
                                <Col xs={24} md={16}>
                                    <div className="order-items">
                                        <Title level={3}>Order Items</Title>
                                        {cartData?.cartItems.map((item) => (
                                            <div key={item.id} className="item-card">
                                                <Image
                                                    className="item-image"
                                                    src={item.product.mainImage}
                                                    alt={item.product.name}
                                                    width={80}
                                                />
                                                <div className="item-details">
                                                    <Title level={5} className="item-name">
                                                        {item.product.name} / {item.size.name}
                                                    </Title>
                                                    <div className="item-price">
                                                        <Text className="discounted-price">
                                                            {(item.product.price * (1 - item.product.discountPercentage / 100)).toLocaleString()}₫
                                                        </Text>
                                                        <Text delete className="original-price">
                                                            {item.product.price.toLocaleString()}₫
                                                        </Text>
                                                    </div>
                                                    <Text>Quantity: {item.quantity}</Text>
                                                </div>
                                                <div className="item-total">
                                                    <Text strong>{calculateItemTotal(item).toLocaleString()}₫</Text>
                                                </div>
                                            </div>
                                        ))}
                                    </div>

                                    <Title level={4} className="section-title">Shipping Address</Title>
                                    <div className="flex justify-between gap-2">
                                        <Form.Item
                                            validateStatus={errors.shippingAddressId && touched.shippingAddressId ? "error" : ""}
                                            help={touched.shippingAddressId && errors.shippingAddressId}
                                            className="mb-0 flex-4"
                                        >
                                            <Field name="shippingAddressId">
                                                {({ field, form }: FieldProps) => (
                                                    <Select
                                                        {...field}
                                                        placeholder="Select shipping address"
                                                        className="w-100"
                                                        dropdownRender={(menu) => (
                                                            <>
                                                                {menu}
                                                                <Divider />
                                                                <Button
                                                                    type="link"
                                                                    icon={<PlusOutlined />}
                                                                    onClick={() => setShowShippingAddressModal(true)}
                                                                    className="w-100"
                                                                >
                                                                    Add New Address
                                                                </Button>
                                                            </>
                                                        )}
                                                        onBlur={() => form.setFieldTouched('shippingAddressId', true)}
                                                        onChange={(value) => {
                                                            handleAddressChange(value, form)
                                                        }}
                                                        value={field.value}

                                                    >
                                                        {shippingAddressData?.result?.map(address => (
                                                            <Select.Option key={address.id} value={address.id} >
                                                                {address.addressDetail} ({address.recipientName})
                                                            </Select.Option>
                                                        ))}
                                                    </Select>
                                                )}
                                            </Field>
                                        </Form.Item>

                                        <Button
                                            type="default"
                                            danger
                                            icon={<VoucherIcon />}
                                            onClick={() => setShowDiscountModal(true)}
                                            className="flex-1 mb-3"
                                        >
                                            {selectedDiscount ? `Applied Discount:` : 'Apply Discount'}
                                        </Button>
                                    </div>
                                </Col>

                                <Col xs={24} md={8}>
                                    <div>
                                        <Title level={4}>Order Summary</Title>
                                        <div className="summary-row">
                                            <Text>Subtotal:</Text>
                                            <Text>{calculateSubtotal().toLocaleString()}₫</Text>
                                        </div>

                                        {selectedDiscount && (
                                            <div className="summary-row">
                                                <Text>Discount:</Text>
                                                <Text type="danger">
                                                    -{calculateDiscountAmount().toLocaleString()}₫
                                                </Text>
                                            </div>
                                        )}

                                        <div className="summary-row">
                                            <Text>Shipping Fee:</Text>
                                            <Text>{shippingFee.toLocaleString()}₫</Text>
                                        </div>

                                        <Divider />

                                        <div className="summary-row total">
                                            <Text>Total:</Text>
                                            <Text>{calculateTotal().toLocaleString()}₫</Text>
                                        </div>

                                        <Button
                                            type="primary"
                                            danger
                                            size="large"
                                            htmlType="submit"
                                            block
                                        >
                                            Complete Order
                                        </Button>
                                    </div>
                                </Col>
                            </Row>
                        </FormikForm>
                    )}
                </Formik>
            )

            }

            <CreateOrUpdateShippingAddressModal
                isOpen={showShippingAddressModal}
                onClose={() => setShowShippingAddressModal(false)}
                onSuccess={() => {
                    refetch();
                    setShowShippingAddressModal(false);
                }}
            />

            {showDiscountModal && (
                <DiscountModal
                    onClose={() => setShowDiscountModal(false)}
                    onOke={(discount) => {
                        setSelectedDiscount(discount);
                        setShowDiscountModal(false);
                    }}
                    selectedDiscount={selectedDiscount}
                    total={calculateSubtotal()}
                />
            )}
        </div>
    );
};

export default Checkout;