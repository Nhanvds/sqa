import { useSelector } from 'react-redux';
import { Navigate, Outlet } from 'react-router-dom';
import {RootState} from "../../redux/store";

const ProtectedRoute = () => {
    const userInfo = useSelector((state: RootState) => state.user.user);
    if (userInfo) {
        return <Outlet />;
    }

    return <Navigate to="/account/login" replace />;
};

export default ProtectedRoute;