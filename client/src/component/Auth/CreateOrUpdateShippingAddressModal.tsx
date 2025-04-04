import {useCreateShippingAddressMutation, useUpdateShippingAddressMutation} from "../../redux/api/shipping-address";
import {RootState} from "../../redux/store";
import {useSelector} from "react-redux";
import * as Yup from 'yup';
import {Button, Checkbox, Form, Input, Modal, Select} from "antd";
import {ShippingAddressResponse} from "../../types/shipping-address";
import {Field, FieldProps, Form as FormikForm, Formik} from 'formik';
import {PhoneOutlined, UserOutlined} from '@ant-design/icons';
import "../../sass/shipping-address.scss";
import {showCustomNotification} from "../../utils/notification";
import {useEffect, useState} from "react";
import axios from "axios";

const {Option} = Select;

const ShippingAddressSchema = Yup.object().shape({
    recipientName: Yup.string().required("Recipient Name is required"),
    phoneNumber: Yup.string().required("Phone Number is required"),
    addressDetail: Yup.string().required("Address Detail is required"),
    country: Yup.string().required("Country is required"),
    city: Yup.string().required("City is required"),
    district: Yup.string().required("District is required"),
    ward: Yup.string().required("Ward is required"),
});

interface ShippingAddress {
    recipientName: string;
    phoneNumber: string;
    addressDetail: string;
    city: string;
    district: string;
    ward: string;
    country: string;
    isDefault: boolean;
}

interface ShippingAddressFormProps {
    editingAddress?: ShippingAddressResponse;
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

enum CodeAddress {
    CITY = "1",
    DISTRICT = "2",
    WARD = "3",
}

const getURLAddress = (codeAddress: CodeAddress, id: string) => {
    return `https://esgoo.net/api-tinhthanh/${codeAddress}/${id}.htm`;
}

interface AddressItem {
    id: string;
    name: string;
}


const CreateOrUpdateShippingAddressModal: React.FC<ShippingAddressFormProps> = ({
                                                                                    editingAddress,
                                                                                    isOpen,
                                                                                    onClose,
                                                                                    onSuccess,
                                                                                }) => {
    const userInfo = useSelector((state: RootState) => state.user.user);
    const [createShippingAddress] = useCreateShippingAddressMutation();
    const [updateShippingAddress] = useUpdateShippingAddressMutation();

    const [provinces, setProvinces] = useState<AddressItem[]>([]);
    const [districts, setDistricts] = useState<AddressItem[]>([]);
    const [wards, setWards] = useState<AddressItem[]>([]);

    useEffect(() => {
        axios.get(getURLAddress(CodeAddress.CITY, "0"))
            .then((response) => {
                if (response.data.error === 0) {
                    setProvinces(response.data.data);
                }
            })
            .catch((error) => console.error("Error fetching provinces:", error));
    }, []);

    const handleProvinceChange = (provinceName: string) => {
        setDistricts([]);
        setWards([]);
        const provinceId = provinces.find((province) => province.name === provinceName)?.id ?? "";

        axios.get(getURLAddress(CodeAddress.DISTRICT, provinceId))
            .then((response) => {
                if (response.data.error === 0) {
                    setDistricts(response.data.data);
                }
            })
            .catch((error) => console.error("Error fetching districts:", error));
    };

    const handleDistrictChange = (districtName: string) => {
        setWards([]);
        const districtId = districts.find((district) => district.name === districtName)?.id ?? "";
        axios.get(getURLAddress(CodeAddress.WARD, districtId))
            .then((response) => {
                if (response.data.error === 0) {
                    setWards(response.data.data);
                }
            })
            .catch((error) => console.error("Error fetching wards:", error));
    };

    const initialValues: ShippingAddress = {
        recipientName: editingAddress?.recipientName ?? "",
        phoneNumber: editingAddress?.phoneNumber ?? "",
        city: editingAddress?.city ?? "",
        district: editingAddress?.district ?? "",
        ward: editingAddress?.ward ?? "",
        addressDetail: editingAddress?.addressDetail ?? "",
        country: editingAddress?.country ?? "Viet Nam",
        isDefault: editingAddress?.isDefault ?? false,
    };

    const handleFormSubmit = async (values: ShippingAddress) => {
        if (editingAddress) {
            await updateShippingAddress({
                id: editingAddress.id,
                shippingAddressRequest: {
                    userId: userInfo?.id ?? "",
                    ...values,
                }
            }).unwrap().then((result) => {
                showCustomNotification({
                    type: "success",
                    message: result?.message ?? "Update shipping address successfully",
                });
                onSuccess();
            }).catch((error) => {
                showCustomNotification({
                    type: "error",
                    message: error.data.message,
                });
            });
        } else {
            await createShippingAddress({
                userId: userInfo?.id ?? "",
                ...values,
            }).unwrap().then((result) => {
                showCustomNotification({
                    type: "success",
                    message: result?.message ?? "Create shipping address successfully",
                });
                onSuccess();
            }).catch((error) => {
                showCustomNotification({
                    type: "error",
                    message: error.data.message,
                });
            });
            ;
        }
    };

    return (
        <Modal
            title={editingAddress ? "Edit Address" : "Add New Address"}
            open={isOpen}
            onCancel={onClose}
            footer={null}
            width={500}
        >
            <Formik
                initialValues={initialValues}
                validationSchema={ShippingAddressSchema}
                onSubmit={handleFormSubmit}
                enableReinitialize
            >
                {({errors, touched}) => (
                    <FormikForm>
                        <Form.Item
                            validateStatus={errors.recipientName && touched.recipientName ? 'error' : ''}
                            help={touched.recipientName && errors.recipientName}
                            className="mb-2"
                        >
                            <Field name="recipientName">
                                {({field}: FieldProps) => (
                                    <Input
                                        {...field}
                                        placeholder="Enter recipient name"
                                        prefix={<UserOutlined className="mr-1"/>}
                                    />
                                )}
                            </Field>
                        </Form.Item>

                        <Form.Item
                            validateStatus={errors.phoneNumber && touched.phoneNumber ? 'error' : ''}
                            help={touched.phoneNumber && errors.phoneNumber}
                            className="mb-2"
                        >
                            <Field name="phoneNumber">
                                {({field}: FieldProps) => (
                                    <Input
                                        {...field}
                                        placeholder="Enter phone number"
                                        prefix={<PhoneOutlined className="mr-1"/>}
                                    />
                                )}
                            </Field>
                        </Form.Item>

                        <Form.Item
                            validateStatus={errors.country && touched.country ? 'error' : ''}
                            help={touched.country && errors.country}
                            className="mb-2"
                        >
                            <Field name="country">
                                {({field, form}: FieldProps) => (
                                    <Select placeholder="Select country"
                                            onChange={(value) => form.setFieldValue('country', value)}
                                            onBlur={() => form.setFieldTouched('country', true)}
                                            value={field.value || "Vietnam"}
                                    >
                                        <Select.Option value="Vietnam">Vietnam</Select.Option>
                                    </Select>
                                )}
                            </Field>
                        </Form.Item>

                        <Form.Item
                            validateStatus={errors.city && touched.city ? 'error' : ''}
                            help={touched.city && errors.city}
                            className="mb-2"
                        >
                            <Field name="city">
                                {({field, form}: FieldProps) => (
                                    <Select
                                        placeholder="Select city"
                                        onChange={(value) => {
                                            form.setFieldValue("city", value);
                                            form.setFieldValue("district", "");
                                            form.setFieldValue("ward", "");
                                            handleProvinceChange(value);
                                        }}
                                        onBlur={() => form.setFieldTouched('city', true)}
                                        value={field.value || undefined}
                                    >
                                        {provinces.map((province) => (
                                            <Option key={province.id} value={province.name}>
                                                {province.name}
                                            </Option>
                                        ))}
                                    </Select>
                                )}
                            </Field>
                        </Form.Item>

                        <Form.Item
                            validateStatus={errors.district && touched.district ? 'error' : ''}
                            help={touched.district && errors.district}
                            className="mb-2"
                        >
                            <Field name="district">
                                {({field, form}: FieldProps) => (
                                    <Select
                                        placeholder="Select district"
                                        onChange={(value) => {
                                            form.setFieldValue("district", value);
                                            form.setFieldValue("ward", "");
                                            handleDistrictChange(value);
                                        }}
                                        onBlur={() => form.setFieldTouched('district', true)}
                                        value={field.value || undefined}
                                        disabled={!form.values.city}
                                    >
                                        {districts.map((district) => (
                                            <Option key={district.id} value={district.name}>
                                                {district.name}
                                            </Option>
                                        ))}
                                    </Select>
                                )}
                            </Field>
                        </Form.Item>

                        <Form.Item
                            validateStatus={errors.ward && touched.ward ? 'error' : ''}
                            help={touched.ward && errors.ward}
                            className="mb-2"
                        >
                            <Field name="ward">
                                {({field, form}: FieldProps) => (
                                    <Select
                                        placeholder="Select ward"
                                        onChange={(value) => form.setFieldValue("ward", value)}
                                        onBlur={() => form.setFieldTouched('ward', true)}
                                        value={field.value || undefined}
                                        disabled={!form.values.district || !form.values.city}
                                    >
                                        {wards.map((ward) => (
                                            <Option key={ward.id} value={ward.name}>
                                                {ward.name}
                                            </Option>
                                        ))}
                                    </Select>
                                )}
                            </Field>
                        </Form.Item>

                        <Form.Item
                            validateStatus={errors.addressDetail && touched.addressDetail ? 'error' : ''}
                            help={touched.addressDetail && errors.addressDetail}
                            className="mb-2"
                        >
                            <Field name="addressDetail">
                                {({field}: FieldProps) => (
                                    <Input.TextArea
                                        {...field}
                                        placeholder="Enter address detail"
                                        rows={3}
                                    />
                                )}
                            </Field>
                        </Form.Item>

                        <Form.Item className="mb-2">
                            <Field name="isDefault">
                                {({field}: FieldProps) => (
                                    <Checkbox {...field} checked={field.value}
                                    >
                                        Set as default address
                                    </Checkbox>
                                )}
                            </Field>
                        </Form.Item>

                        <Form.Item className="mb-2">
                            <Button type="primary" htmlType="submit" block>
                                {editingAddress ? "Update" : "Add"} Address
                            </Button>
                        </Form.Item>

                        <Form.Item className="mb-2">
                            <Button block onClick={onClose}>
                                Cancel
                            </Button>
                        </Form.Item>
                    </FormikForm>
                )}
            </Formik>
        </Modal>
    );
};

export default CreateOrUpdateShippingAddressModal;