import {Row, Col, Typography, Card, Button, Spin} from 'antd';
import {useDispatch, useSelector} from 'react-redux';
import {RootState} from "../../redux/store";
import {clearUser} from "../../redux/slice/userSlice";
import {useNavigate} from "react-router-dom";
import {LogoutOutlined, PlusCircleOutlined} from "@ant-design/icons";
import { useGetOrdersByUserIdQuery } from '../../redux/api/order';
import OrderList from '../Home/OrderList';
import { useEffect } from 'react';

const {Title, Text} = Typography;

export const Account = () => {

    const userInfo = useSelector((state: RootState) => state.user.user);
    const {data: getOrder, isFetching: orderFetching, refetch: refetchOrder} = useGetOrdersByUserIdQuery(userInfo?.id ?? "");

    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleLogout = () => {
        dispatch(clearUser());
        localStorage.removeItem('token');
        navigate('/account/login');
    }

    useEffect(() => {
        if(!userInfo) {
            navigate('/account/login');
        }
    }, [userInfo]);

    return (
        <>
            <div className="flex justify-space-between w-100">
                <Title level={4} className="m-0 fw-500">Order Account</Title>
                <Button type="link" onClick={handleLogout} className="fw-600">
                    <LogoutOutlined/>
                    Logout
                </Button>
            </div>
            <Row gutter={[24, 24]} justify="center">
                <Col xs={24} sm={24} md={16} lg={16} xl={16}>
                    {orderFetching ? (
                        <div className="flex justify-center w-100 mt-3">
                            <Spin size="large" />
                        </div>
                    ) : (
                        getOrder?.result ? (
                                <OrderList orderList={getOrder.result} refetchOrder={refetchOrder}/>
                        ) : (
                                <Card>
                                    <Text>You have not purchased any products.</Text>
                                </Card>
                        )
                    )}
                </Col>

                <Col xs={24} sm={24} md={8} lg={8} xl={8}>
                    <Card>
                        <div className="account-info">
                            <div className="account-info-details">
                                <Text>
                                    <strong>Name</strong>: {userInfo?.name ?? ""}
                                </Text>
                                <br/>
                                <Text>
                                    <strong>Email</strong>: {userInfo?.email ?? ""}
                                </Text>
                                <br/>
                                <Text>
                                    <strong>Phone</strong>: {""}
                                </Text>
                                <br/>
                                <Text>
                                    <strong>Address</strong>: {""}
                                </Text>
                                <br/>
                                <div className="flex justify-end">
                                    <Button type="link" href="/account/address" className="fw-600">
                                        View Address
                                        <PlusCircleOutlined/>
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </Card>
                </Col>
            </Row>
        </>

    );
};