import ProductTable from "@/app/dashboard/product/component/product-table";
import PageContainer from "@/components/layout/page-container";

const ProductPage = () => {
    return (
        <PageContainer scrollable>
            <ProductTable />
        </PageContainer>
    );
}

export default ProductPage;