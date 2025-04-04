import { Button, Form, Input, Modal, Rate } from "antd";
import { useEffect, useState } from "react";
import { useCreateReviewMutation, useUpdateReviewMutation } from "../../redux/api/review";
import { showCustomNotification } from "../../utils/notification";
import { ReviewResponse } from "../../types/review";
import { EditFilled } from "@ant-design/icons";

const CreateReviewModal: React.FC<{
    productId: string;
    orderId: string;
    onSuccess: () => void;
    review?: ReviewResponse;
}> = ({ productId, orderId, onSuccess, review }) => {
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [form] = Form.useForm();
    const [createReview, { isLoading: isCreating }] = useCreateReviewMutation();
    const [updateReview, { isLoading: isUpdating }] = useUpdateReviewMutation();

    useEffect(() => {
        if (isModalVisible && review) {
            form.setFieldsValue({
                rating: review.rating,
                content: review.content,
            });
        }
    }, [isModalVisible, review, form]);

    const handleSubmit = (values: { rating: number; content: string }) => {
        if (review) {
            updateReview({
                id: review.id,
                review: {
                    productId,
                    orderId,
                    rating: values.rating,
                    content: values.content,
                }
            }).unwrap()
                .then((res) => {
                    showCustomNotification({
                        type: "success",
                        message: res.message
                    });
                    form.resetFields();
                    setIsModalVisible(false);
                    onSuccess();
                })
                .catch((error) => {
                    showCustomNotification({
                        type: "error",
                        message: error.data.message
                    });
                });
        } else {
            createReview({
                productId,
                orderId,
                rating: values.rating,
                content: values.content,
            }).unwrap()
                .then((res) => {
                    showCustomNotification({
                        type: "success",
                        message: res.message
                    });
                    form.resetFields();
                    setIsModalVisible(false);
                    onSuccess();
                })
                .catch((error) => {
                    showCustomNotification({
                        type: "error",
                        message: error.data.message
                    });
                });
        }
    };

    return (
        <>
            <Button
                type="primary"
                ghost
                icon={<EditFilled />}
                onClick={() => setIsModalVisible(true)}
            >
                {review ? "Edit Review" : "Write a Review"}
            </Button>

            <Modal
                title={review ? "Edit Review" : "Write a Review"}
                open={isModalVisible}
                onCancel={() => {
                    setIsModalVisible(false);
                    form.resetFields();
                }}
                footer={null}
            >
                <Form
                    form={form}
                    layout="vertical"
                    onFinish={handleSubmit}
                    initialValues={{
                        rating: review?.rating || 0,
                        content: review?.content || "",
                    }}
                >
                    <Form.Item
                        name="rating"
                        label="Rating"
                        rules={[
                            {
                                required: true,
                                message: 'Please rate the product',
                            },
                        ]}
                    >
                        <Rate
                            allowHalf
                            tooltips={[
                                'Terrible',
                                'Bad',
                                'Normal',
                                'Good',
                                'Excellent',
                            ]}
                        />
                    </Form.Item>

                    <Form.Item
                        name="content"
                        label="Review Details"
                        rules={[
                            {
                                required: true,
                                message: 'Please write your review',
                            },
                        ]}
                    >
                        <Input.TextArea
                            rows={4}
                            placeholder="Share your experience with this product..."
                            showCount
                            maxLength={500}
                        />
                    </Form.Item>

                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            loading={isCreating || isUpdating}
                            block
                        >
                            Submit Review
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
};

export default CreateReviewModal;