import { useMemo, useState } from "react";
import { OrderResponse } from "../../types/order";
import { List, Tabs, TabsProps } from "antd";
import OrderItem from "./OrderItem";
import { OrderStatusEnum } from "../../types/enums";
import "../../sass/order.scss";

interface OrderListProps {
    orderList: OrderResponse[];
    refetchOrder: () => void;
}

const OrderList: React.FC<OrderListProps> = ({ orderList, refetchOrder }) => {
    const [activeTab, setActiveTab] = useState('ALL');

    const filteredOrders = useMemo(() => {
        if (activeTab === 'ALL') return orderList;
        return orderList.filter(order => order.status === activeTab);
    }, [orderList, activeTab]);

    const items: TabsProps['items'] = [
        {
            key: 'ALL',
            label: `ALL ORDER (${orderList.length})`,
            children: (
                <List
                    dataSource={filteredOrders}
                    renderItem={(order) => (
                        <OrderItem order={order} />
                    )}
                    locale={{ emptyText: 'No orders found' }}
                />
            ),
        },
        ...Object.values(OrderStatusEnum).map(status => ({
            key: status,
            label: `${status} (${orderList.filter(order => order.status === status).length})`,
            children: (
                <List
                    dataSource={filteredOrders}
                    renderItem={(order) => (
                        <OrderItem order={order} />
                    )}
                    locale={{ emptyText: 'No orders found' }}
                />
            ),
        })),
    ];

    return (
        <div className="order-list">
            <Tabs
                defaultActiveKey="ALL"
                items={items}
                onChange={setActiveTab}
                className="order-tabs"
                type="card"
            />
        </div>
    );
};

export default OrderList;