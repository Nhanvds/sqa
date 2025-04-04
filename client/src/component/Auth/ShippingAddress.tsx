import {useDeleteShippingAddressMutation, useGetShippingAddressByUserIdQuery,} from "../../redux/api/shipping-address";
import {RootState} from "../../redux/store";
import {useSelector} from "react-redux";
import {useState} from "react";
import {Button, Card, Col, Row, Space, Typography} from "antd";
import {ShippingAddressResponse} from "../../types/shipping-address";
import {
    DeleteOutlined,
    EditOutlined,
    EnvironmentOutlined,
    GlobalOutlined,
    HomeOutlined,
    PhoneOutlined,
    PlusOutlined
} from '@ant-design/icons';
import "../../sass/shipping-address.scss";
import {showCustomNotification} from "../../utils/notification";
import ConfirmModal from "../../utils/ConfirmModal";
import CreateOrUpdateShippingAddressModal from "./CreateOrUpdateShippingAddressModal";

const {Text, Title} = Typography;

const ShippingAddress = () => {
    const userInfo = useSelector((state: RootState) => state.user.user);
    const [deleteShippingAddress] = useDeleteShippingAddressMutation();
    const {data: shippingAddressData, refetch} = useGetShippingAddressByUserIdQuery(userInfo?.id ?? "");
    const [createOrEditShippingAddressModalVisible, setCreateOrEditShippingAddressModalVisible] = useState<{
        visible: boolean;
        editingAddress: ShippingAddressResponse | null;
    }>({
        visible: false,
        editingAddress: null,
    });
    const [showConfirmDeleteModal, setShowConfirmDeleteModal] = useState<{
        isOpen: boolean;
        addressId: string;
    }>({
        isOpen: false,
        addressId: "",
    });

    const handleDelete = async (id: string) => {
        await deleteShippingAddress(id).unwrap().then((result) => {
            showCustomNotification({
                type: "success",
                message: result?.message ?? "Delete shipping address successfully",
            });
            refetch();
        }).catch((error) => {
            showCustomNotification({
                type: "error",
                message: error.data.message,
            });
        });
        ;
    };

    return (
        <div className="p-3">
            <Row justify="space-between" align="middle" className="mb-2">
                <Title level={3}>Shipping Addresses</Title>
                <Button
                    type="primary"
                    icon={<PlusOutlined/>}
                    onClick={() => {
                        setCreateOrEditShippingAddressModalVisible({
                            visible: true,
                            editingAddress: null,
                        });
                    }}
                >
                    Add New Address
                </Button>
            </Row>

            <Row gutter={[16, 16]}>
                {shippingAddressData?.result?.map((address) => (
                    <Col xs={24} sm={12} md={8} key={address.id}>
                        <Card
                            hoverable
                            className="address-card"
                            actions={[
                                <Button
                                    icon={<EditOutlined/>}
                                    type="link"
                                    onClick={() => {
                                        setCreateOrEditShippingAddressModalVisible({
                                            visible: true,
                                            editingAddress: address,
                                        })
                                    }}
                                >
                                    Edit
                                </Button>,
                                <Button
                                    icon={<DeleteOutlined/>}
                                    type="link"
                                    danger
                                    onClick={() => setShowConfirmDeleteModal({isOpen: true, addressId: address.id})}
                                >
                                    Delete
                                </Button>,
                            ]}
                        >
                            <Space direction="vertical" size="middle" className="w-100">
                                <div>
                                    <Text strong className="fs-16">{address.recipientName}</Text>
                                    {address.isDefault && (
                                        <Text type="success" className="ml-1">
                                            (Default)
                                        </Text>
                                    )}
                                </div>

                                <Space direction="vertical" size="small">
                                    <Space>
                                        <PhoneOutlined/>
                                        <Text>{address.phoneNumber}</Text>
                                    </Space>
                                    <Space>
                                        <HomeOutlined/>
                                        <Text>{address.addressDetail}</Text>
                                    </Space>
                                    <Space>
                                        <EnvironmentOutlined/>
                                        <Text>{`${address.ward} - ${address.district} - ${address.city}`}</Text>
                                    </Space>
                                    <Space>
                                        <GlobalOutlined/>
                                        <Text>{address.country}</Text>
                                    </Space>
                                </Space>
                            </Space>
                        </Card>
                    </Col>
                ))}
            </Row>

            <CreateOrUpdateShippingAddressModal
                isOpen={createOrEditShippingAddressModalVisible.visible}
                editingAddress={createOrEditShippingAddressModalVisible.editingAddress ?? undefined}
                onClose={() => setCreateOrEditShippingAddressModalVisible({visible: false, editingAddress: null})}
                onSuccess={() => {
                    refetch();
                    setCreateOrEditShippingAddressModalVisible({visible: false, editingAddress: null});
                }}
            />

            <ConfirmModal
                isOpen={showConfirmDeleteModal.isOpen}
                onClose={() => setShowConfirmDeleteModal({isOpen: false, addressId: ""})}
                onConfirm={() => {
                    handleDelete(showConfirmDeleteModal.addressId);
                    setShowConfirmDeleteModal({isOpen: false, addressId: ""});
                }}
                title="Delete Address"
                message="Are you sure you want to delete this address?"
            />
        </div>
    );
};

export default ShippingAddress;
