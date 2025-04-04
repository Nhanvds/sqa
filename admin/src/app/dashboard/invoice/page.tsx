import InvoiceTable from "@/app/dashboard/invoice/invoice-table";
import PageContainer from "@/components/layout/page-container";

const InvoicePage = () => {
    return (
        <PageContainer scrollable>
            <InvoiceTable />
        </PageContainer>
    );
}

export default InvoicePage;