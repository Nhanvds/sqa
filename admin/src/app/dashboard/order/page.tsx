import OrderTable from "@/app/dashboard/order/component/order-table";
import PageContainer from "@/components/layout/page-container";

const OrderPage = () => {

    return (
        <PageContainer scrollable>
            <OrderTable />
        </PageContainer>
    )
}

export default OrderPage;