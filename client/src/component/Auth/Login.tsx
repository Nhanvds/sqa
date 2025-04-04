import React, { useEffect } from 'react';
import { Form, Input, Button, Row, Col, Checkbox, Typography } from 'antd';
import { Formik, Field, Form as FormikForm, FieldProps } from 'formik';
import * as Yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { baseApi, useLoginEmailMutation } from "../../redux/api/auth-api";
import { setUser } from "../../redux/slice/userSlice";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../redux/store";
import { useLazyGetUserInfoQuery } from "../../redux/api/user-api";
import { LockOutlined, MailOutlined } from "@ant-design/icons";
import { ReactComponent as GoogleIcon } from "../../img/svg/google.svg";
import { showCustomNotification } from '../../utils/notification';

const { Text, Title, Link } = Typography;

interface FormValues {
    email: string;
    password: string;
}

const validationSchema = Yup.object().shape({
    email: Yup.string()
        .email('Invalid email')
        .required('Please enter your email'),
    password: Yup.string()
        .required('Please enter your password')
});

const Login: React.FC = () => {
    const [login] = useLoginEmailMutation();
    const navigate = useNavigate();

    const initialValues: FormValues = {
        email: '',
        password: '',
    };

    const dispatch = useDispatch();
    const userInfo = useSelector((state: RootState) => state.user.user);
    const [getUserInfo] = useLazyGetUserInfoQuery();

    const handleGetUserInfo = () => {
        getUserInfo().unwrap()
            .then((res) => {
                if (res?.result) {
                    dispatch(setUser(res.result));
                }
            })
            .catch((error) => {
                showCustomNotification({
                    type: "error",
                    message: error.data?.message || "Failed to get user info",
                });
            });
    }

    const handleSubmit = (values: FormValues) => {
        login({
            email: values.email,
            password: values.password,
        }).unwrap().then((res) => {
            localStorage.setItem("token", res.result?.token ?? "");
            if (!userInfo) {
                handleGetUserInfo();
            }
            navigate('/account');
        }).catch((error) => {
            showCustomNotification({
                type: "error",
                message: error.data?.message || "Login failed",
            });
        });
    };

    const handleGoogleLogin = async () => {
        window.location.href = `${baseApi}/oauth2/login/google`;
    };

    useEffect(() => {
        if (userInfo) {
            navigate('/account');
        }
    }, [userInfo]);

    return (
        <Row align="middle" justify="center" gutter={[24, 24]} className="h-100">
            <Col
                xs={20} sm={20} md={8} lg={8} xl={8}
                className="flex justify-center align-center border"
            >

                <Formik
                    initialValues={initialValues}
                    validationSchema={validationSchema}
                    onSubmit={handleSubmit}
                >
                    {({ errors, touched }) => (
                        <FormikForm className="login-form">
                            <Title level={5} className="text-center mt-0 pb-2">Login</Title>
                            <Form.Item
                                validateStatus={errors.email && touched.email ? 'error' : ''}
                                help={touched.email && errors.email}
                                style={{ marginBottom: "24px" }}
                            >
                                <Field name="email">
                                    {({ field }: FieldProps) => (
                                        <Input
                                            {...field}
                                            placeholder="Email"
                                            prefix={<MailOutlined className="mr-1" />}
                                        />
                                    )}
                                </Field>
                            </Form.Item>

                            <Form.Item
                                validateStatus={errors.password && touched.password ? 'error' : ''}
                                help={touched.password && errors.password}
                                style={{ marginBottom: "24px" }}
                            >
                                <Field name="password">
                                    {({ field }: FieldProps) => (
                                        <Input.Password
                                            {...field}
                                            placeholder="Password"
                                            prefix={<LockOutlined className="mr-1" />}
                                        />
                                    )}
                                </Field>
                            </Form.Item>

                            <Form.Item className='mb-2'>
                                <Checkbox>Remember me</Checkbox>
                                <Link style={{ float: "right" }} href="#">
                                    Forgot password?
                                </Link>
                            </Form.Item>

                            <Form.Item className='mb-2'>
                                <Button type="primary" htmlType="submit" className="fw-600" block>
                                    Login
                                </Button>
                            </Form.Item>

                            <Form.Item className='mb-2'>
                                <Button
                                    type="default"
                                    onClick={handleGoogleLogin}
                                    block
                                    icon={<GoogleIcon className="fs-12" />}
                                    className="fw-600 background-google"
                                >
                                    Login with Google
                                </Button>
                            </Form.Item>

                            <Form.Item style={{ textAlign: "center" }}>
                                <Text>Don't have an account?</Text> <Link href="/account/register">Sign up now</Link>
                            </Form.Item>
                        </FormikForm>
                    )}
                </Formik>
            </Col>
        </Row>
    );
};

export default Login;
