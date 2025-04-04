import React, {useState} from "react";
import {Checkbox, Divider, Input, List, Modal, Typography} from "antd";
import {useSelector} from "react-redux";
import {RootState} from "../../redux/store";
import {useGetAutoApplyQuery, useLazyGetDiscountQuery} from "../../redux/api/discount";
import {DiscountResponse} from "../../types/discount";
import {DiscountTypeEnum} from "../../types/enums";
import DebouncedInput from "../../utils/DebouncedInput";
import { CloseCircleFilled } from "@ant-design/icons";

const {Text} = Typography;

interface DiscountModalProps {
    onClose: () => void;
    onOke: (discount: DiscountResponse | undefined) => void;
    selectedDiscount: DiscountResponse | undefined;
    total: number;
}

const DiscountModal = ({onClose, selectedDiscount, onOke, total}: DiscountModalProps) => {
    const userInfo = useSelector((state: RootState) => state.user.user);
    const {data: autoApplyDiscounts} = useGetAutoApplyQuery(userInfo?.id ?? "");
    const [getDiscountByCode, {data: discountByCode}] = useLazyGetDiscountQuery();
    const [searchCode, setSearchCode] = useState("");
    const discounts = discountByCode?.result ? [discountByCode.result] : autoApplyDiscounts?.result ?? [];
    const [pickedDiscount, setPickedDiscount] = useState<DiscountResponse | undefined>(selectedDiscount);

    const handleSearch = () => {
        if (searchCode.trim()) {
            getDiscountByCode({code: searchCode, userId: userInfo?.id ?? ""});
        }
    };

    return (
        <Modal
            open={true}
            onCancel={onClose}
            onOk={() => {
                onOke(pickedDiscount);
            }}
            title="Choose a Discount"
        >
            <Input.Search
                placeholder="Search discount code"
                value={searchCode}
                onChange={(e) => setSearchCode(e.target.value)}
                onSearch={handleSearch}
                enterButton="Search"
                className="mb-2"
                suffix={
                    searchCode? (
                        <CloseCircleFilled
                            onClick={() => setSearchCode('')}
                            className='cursor-pointer text-color-secondary'
                        />
                    ) : null
                }
            />
            <Divider/>
            <Text strong>Available Discounts:</Text>
            {discounts.length === 0 ? (
                <Text>No discounts available.</Text>
            ) : (
                <List
                    dataSource={discounts}
                    renderItem={(item) => (
                        <List.Item>
                            <Checkbox
                                checked={item.id === pickedDiscount?.id}
                                onChange={() => setPickedDiscount(item.id === pickedDiscount?.id ? undefined : item)}
                                disabled={total < item.minOrderValue}
                                className="mr-2"
                            />
                            <List.Item.Meta
                                title={
                                    <>
                                        <Text strong>
                                            {item.discountType === DiscountTypeEnum.PERCENTAGE
                                                ? `${item.discountPercentage}% off`
                                                : `${item?.discountValue?.toLocaleString()} VND discount`}
                                        </Text>
                                        <Text style={{marginLeft: 8}}>
                                            (Max: {item.maxDiscountValue.toLocaleString()} VND)
                                        </Text>
                                    </>
                                }
                                description={
                                    <>
                                        <Text>
                                            Minimum order:{" "}
                                            <Text strong>{item.minOrderValue.toLocaleString()} VND</Text>
                                        </Text>
                                        <br/>
                                        <Text>
                                            Expires on:{" "}
                                            <Text strong>
                                                {item.expiryDate
                                                    ? new Date(item.expiryDate).toLocaleDateString()
                                                    : "No expiry"}
                                            </Text>
                                        </Text>
                                    </>
                                }
                            />
                        </List.Item>
                    )}
                />
            )}
        </Modal>
    );
};

export default DiscountModal;