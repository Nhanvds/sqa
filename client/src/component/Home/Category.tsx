import {Menu} from "antd";
import {CategoryResponse} from "../../types/category";
import {useState} from "react";
import '../../sass/home-page.scss';
import { useNavigate, useSearchParams } from "react-router-dom";

interface CategoryProps {
    categories: CategoryResponse[];
}

const Category: React.FC<CategoryProps> = ({ categories }) => {
    const [searchParams, setSearchParams] = useSearchParams();

    const onCategorySelect = (categoryId: string) => {
        const search = searchParams.get('search') || '';
        setSearchParams({ categoryId, search });
    };

    return (
        <Menu
            mode="inline"
            defaultSelectedKeys={['1']}
            defaultOpenKeys={['sub1']}
            className="h-100"
            onClick={(e) => onCategorySelect(e.key)}
        >
            <Menu.SubMenu
                key="sub1"
                title={
                    <span className="fs-18 fw-600 text-color">
                        Categories
                    </span>
                }
                className="background-card"
            >
                <Menu.Divider />
                {
                    categories.map((category) => (
                        <Menu.Item key={category.id}>
                            <span className="fs-14 fw-600 text-primary">{category.name.toUpperCase()}</span>
                        </Menu.Item>
                    ))}
            </Menu.SubMenu>
        </Menu>
    );
};

export default Category;