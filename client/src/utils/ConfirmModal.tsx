import { Button, Modal, Typography } from "antd";
import { WarningOutlined } from "@ant-design/icons";

interface ConfirmModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    title: string;
    message: string;
    icon?: React.ReactNode;
    confirmButtonText?: string;
    cancelButtonText?: string;
    isSubmitLoading?: boolean;
}

const ConfirmModal = ({
    isOpen,
    onClose,
    onConfirm,
    isSubmitLoading,
    title,
    message,
    icon = <WarningOutlined className="text-error fs-32" />,
    confirmButtonText = "Remove",
    cancelButtonText = "Cancel",
}: ConfirmModalProps) => {
    return (
        <Modal
            open={isOpen}
            onCancel={onClose}
            footer={null}
            centered
        >
            <div className="text-center">
                <div className="flex justify-center">
                    <div className="p-2 bg-error rounded">
                        {icon}
                    </div>
                </div>
                <Typography.Title level={3} className="mb-2">
                    {title}
                </Typography.Title>
                <Typography.Text className="mb-3 block fs-16">
                    {message}
                </Typography.Text>
                <div className="flex justify-center">
                    <Button onClick={onClose} className="mr-2">
                        {cancelButtonText}
                    </Button>
                    <Button onClick={onConfirm} type="primary" danger loading={isSubmitLoading}>
                        {confirmButtonText}
                    </Button>
                </div>
            </div>
        </Modal>
    );
};

export default ConfirmModal;
