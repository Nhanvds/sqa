import React, { useEffect, useState } from 'react';
import { Layout, Menu, Button, Row, Col, Drawer, Image, Badge, Spin, Input, Dropdown, Grid } from 'antd';
import logo from './img/logo.png';
import {
    FacebookOutlined,
    InstagramOutlined,
    PhoneOutlined,
    MailOutlined,
    MenuOutlined, SearchOutlined, UserOutlined, ShoppingCartOutlined,
} from '@ant-design/icons';
import { Route, BrowserRouter as Router, Routes, useNavigate, useSearchParams } from 'react-router-dom';
import Registration from './component/Auth/Register';
import { Account } from "./component/Auth/Account";
import Login from "./component/Auth/Login";
import OAuth2RedirectHandler from "./component/Auth/OAuth2RedirectHandler";
import { useDispatch, useSelector } from "react-redux";
import { useLazyGetUserInfoQuery } from "./redux/api/user-api";
import { setUser } from "./redux/slice/userSlice";
import { RootState } from "./redux/store";
import HomePage from "./component/Home/HomePage";
import ProductDetail from "./component/Home/ProductDetail";
import { useGetCartQuery } from "./redux/api/cart";
import { setCart } from "./redux/slice/cartSlice";
import Cart from "./component/Home/Cart";
import ShippingAddress from "./component/Auth/ShippingAddress";
import Checkout from './component/Home/Checkout';
import Contact from './component/Contact/Contact';
import About from './component/About/About';
import DebouncedInput from './utils/DebouncedInput';
import { useGetAllCategoryQuery } from './redux/api/category-api';
import ChatWidget from './component/Home/ChatWidget';
import AccountActivatedPage from './component/Auth/AccountActivatedPage';

const { Header, Content, Footer } = Layout;
const { useBreakpoint } = Grid;

const App = () => {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const screens = useBreakpoint();

    const userInfo = useSelector((state: RootState) => state.user.user);
    const cart = useSelector((state: RootState) => state.cart.cart);
    const [drawerVisible, setDrawerVisible] = useState(false);
    const [showSearch, setShowSearch] = useState(false);
    const [searchValue, setSearchValue] = useState("");

    const [getUserInfo, { isFetching: isFetchingUser }] = useLazyGetUserInfoQuery();
    const { data: cartData } = useGetCartQuery(userInfo?.id ?? "", {
        skip: !userInfo?.id
    });
    const [searchParams, setSearchParams] = useSearchParams();

    const { data: categoriesData, isFetching: categoriesIsFetching } = useGetAllCategoryQuery();

    const handleChangeSearchValue = (value: string) => {
        setSearchValue(value);
        const categoryId = searchParams.get('categoryId') || '';
        setSearchParams({ categoryId, search: value }); // Sửa từ 'searchValue' thành 'value'
    }

    const renderCategories = () => {
        if (categoriesIsFetching) {
            return <Menu.Item key="loading"><Spin size="small" /></Menu.Item>;
        }

        if (categoriesData?.result && categoriesData.result.length > 0) {
            return categoriesData.result.map((category) => (
                <Menu.Item key={category.id} onClick={() => navigate(`?categoryId=${category.id}`)}>
                    {category.name.toUpperCase()}
                </Menu.Item>
            ));
        }

        return <Menu.Item key="no-categories" disabled>No categories available</Menu.Item>;
    };

    const shopMenu = (
        <Menu>
            {renderCategories()}
        </Menu>
    );

    const handleGetUserInfo = async () => {
        try {
            const result = await getUserInfo().unwrap();
            if (result?.result) {
                dispatch(setUser(result.result));
            }
        } catch (error) {
            console.error("Error getting user info:", error);
        }
    }

    const handleNavigateAccount = () => {
        if (userInfo) {
            navigate('/account');
        } else {
            navigate('/account/login');
        }
    }

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token && !userInfo) {
            void handleGetUserInfo();
        }
    }, [dispatch, getUserInfo, userInfo]);

    useEffect(() => {
        if (cartData?.result) {
            dispatch(setCart(cartData.result));
        }
    }, [cartData?.result, dispatch]);

    // Responsive Drawer for Mobile Menu
    const showDrawer = () => {
        setDrawerVisible(true);
    };

    const closeDrawer = () => {
        setDrawerVisible(false);
    };

    const isMobile = !screens.md;

    return (
        <div className="container">
            <Layout>
                <Header className={`bg-white height-100 ${!isMobile ? "border-radius-10 border-1" : "p-2"}`}>
                    <Row align="middle" justify="center" className="h-100" gutter={[24, 24]}>
                        <Col xs={24} sm={24} md={24} lg={18} xl={18}>
                            <Row align="middle" justify="space-between" gutter={[24, 24]}>
                                <Col xs={4} sm={4} md={4} lg={6} xl={6} className="flex align-center h-100">
                                    {screens.md ?
                                        <>
                                            <Button
                                                type="primary"
                                                shape="circle"
                                                icon={<PhoneOutlined style={{ fontSize: 24 }} />}
                                            />
                                            <span className="text-primary fs-16 fw-600 ml-1">0373357405</span>
                                        </>
                                        : (

                                            <Button
                                                type="text"
                                                shape="circle"
                                                icon={<MenuOutlined className="text-primary" style={{ fontSize: 20 }} />}
                                                onClick={showDrawer}
                                            />

                                        )
                                    }
                                </Col>

                                <Col xs={12} sm={12} md={12} lg={6} xl={6} className="flex justify-center align-center">
                                    <Image
                                        src={logo}
                                        alt="logo"
                                        className="img-min-h-85"
                                        preview={false}
                                        onClick={() => navigate('/')}
                                        style={{ cursor: 'pointer' }}
                                        width={150}
                                    />
                                </Col>

                                <Col xs={8} sm={8} md={8} lg={6} xl={6} className="flex justify-end align-center gap-1">
                                    {!isMobile && (!showSearch ? (
                                        <Button
                                            type="text"
                                            shape="circle"
                                            icon={<SearchOutlined className="text-primary" style={{ fontSize: 24 }} />}
                                            onClick={() => setShowSearch(true)}
                                        />
                                    ) : (
                                        <DebouncedInput
                                            placeholder="Search"
                                            value={searchValue}
                                            onDebouncedChange={handleChangeSearchValue}
                                            delay={500}
                                            className={isMobile ? "w-100" : "max-w-280"}
                                        />
                                    ))}
                                    <Button
                                        type="text"
                                        shape="circle"
                                        icon={<UserOutlined className="text-primary" style={{ fontSize: 24 }} />}
                                        onClick={handleNavigateAccount}
                                    />
                                    <Badge count={cart?.cartItems.length ?? 0} showZero>
                                        <Button
                                            type="text"
                                            shape="circle"
                                            icon={
                                                <ShoppingCartOutlined
                                                    className="text-primary"
                                                    onClick={() => navigate('/cart')}
                                                    style={{ fontSize: 24 }}
                                                />
                                            }
                                        />
                                    </Badge>
                                </Col>
                            </Row>
                        </Col>
                    </Row>

                    <Drawer
                        title="Menu"
                        placement="left"
                        onClose={closeDrawer}
                        visible={drawerVisible}
                        bodyStyle={{ padding: 0 }}
                        width={300}
                    >
                        <Menu mode="vertical" defaultSelectedKeys={['home']} onClick={closeDrawer}>
                            <Menu.Item key="home" onClick={() => navigate('/')}>HOME</Menu.Item>
                            <Menu.SubMenu key="shop" title="SHOP">
                                {renderCategories()}
                            </Menu.SubMenu>
                            <Menu.Item key="blog" onClick={() => navigate('/blog')}>BLOG</Menu.Item>
                            <Menu.Item key="contact" onClick={() => navigate('/contact')}>CONTACT</Menu.Item>
                            <Menu.Item key="about" onClick={() => navigate('/about')}>ABOUT</Menu.Item>
                            <Menu.Item key="group" onClick={() => navigate('/group')}>GROUP</Menu.Item>
                        </Menu>
                    </Drawer>
                </Header>

                <Content className="bg-white w-100">
                    <Row align="middle" justify="center" gutter={[24, 24]} className="my-5">
                        {
                            isFetchingUser ? (
                                <div className="flex justify-center w-100">
                                    <Spin size="large" />
                                </div>
                            ) : (
                                <Col xs={24} sm={24} md={24} lg={24} xl={18}>
                                    <Routes>
                                        <Route path="/" element={<HomePage />} />
                                        <Route path="/product/:id" element={<ProductDetail />} />
                                        <Route path="/cart" element={<Cart />} />
                                        <Route path="/account" element={<Account />}>
                                            <Route index element={<Account />} />
                                        </Route>
                                        <Route path="/contact" element={<Contact />} />
                                        <Route path="account/address" element={<ShippingAddress />} />
                                        <Route path="checkout" element={<Checkout />} />
                                        <Route path="/about" element={<About />} />
                                        <Route path="/account/login" element={<Login />} />
                                        <Route path="/account/register" element={<Registration />} />
                                        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
                                        <Route path="/auth/verify" element={<AccountActivatedPage />} />
                                    </Routes>
                                </Col>
                            )
                        }
                    </Row>
                </Content>

                <Footer className="bg-primary text-white">
                    <Row gutter={[24, 24]} justify="space-around">
                        <Col xs={24} sm={12} md={8} lg={6} xl={6}>
                            <h4>Thông tin liên hệ</h4>
                            <p>
                                <PhoneOutlined /> Phone: 037 335 7405
                            </p>
                            <p>
                                <MailOutlined /> Email: vergency.contact@gmail.com
                            </p>
                        </Col>
                        <Col xs={24} sm={12} md={8} lg={6} xl={6}>
                            <h4>Chính sách hỗ trợ</h4>
                            <ul>
                                <li>Tìm kiếm</li>
                                <li>Giới thiệu</li>
                                <li>Chính sách đổi trả</li>
                                <li>Chính sách bảo mật</li>
                                <li>Điều khoản dịch vụ</li>
                            </ul>
                        </Col>
                        <Col xs={24} sm={12} md={8} lg={6} xl={6}>
                            <h4>Thông tin liên kết</h4>
                            <div>
                                <FacebookOutlined style={{ fontSize: '24px', marginRight: '10px', color: '#1890ff' }} />
                                <InstagramOutlined style={{ fontSize: '24px', color: '#1890ff' }} />
                            </div>
                        </Col>
                    </Row>
                    <div className="text-center mt-4">Bản quyền thuộc về VERGENCY</div>
                </Footer>
            </Layout>

            {userInfo && <ChatWidget />}
        </div>
    );

}

export default App;