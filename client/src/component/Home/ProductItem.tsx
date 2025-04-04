import {ProductResponse} from "../../types/product";
import {Card, Divider, Image, Rate, Typography} from "antd";
import '../../sass/product.scss'
import {useNavigate} from 'react-router-dom';
import '../../sass/home-page.scss';

const { Text, Title } = Typography;

const ProductItem = ({ product }: { product: ProductResponse }) => {
    const navigate = useNavigate();

    return (
        <Card
            hoverable
            cover={<Image alt={product.name} src={product.mainImage} />}
            style={{
                width: 260,
                transition: 'border-radius 0.3s ease'
            }}
            className="relative"
            onMouseEnter={(e) => e.currentTarget.style.borderRadius = '4px'}
            onMouseLeave={(e) => e.currentTarget.style.borderRadius = '0'}
            classNames={{
                body: "p-0-important"
            }}
            onClick={() => navigate(`/product/${product.id}`)}
        >
            <div className="product-discount flex justify-center align-center fs-12">
                {product.discountPercentage}%
            </div>
            <div className="flex flex-column px-2">
                <Title level={5} className="m-0">{product.name}</Title>
                <Text type="secondary">VERGENCY</Text>

                <Rate disabled allowHalf defaultValue={product.rating ?? 5} className="fs-12 color-start" />
            </div>

            <Divider/>

            <div className="flex justify-space-between align-center px-2">
                <Text className="fs-16 text-secondary fw-500">
                    ₫{(100 - product.discountPercentage) / 100 * product.price}
                </Text>
                <br/>
                <Text delete className="fs-16 fw-500">
                    ₫{product.price}
                </Text>
            </div>
        </Card>
    )
}

export default ProductItem;