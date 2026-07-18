import { Navigate } from "react-router-dom";
import { useSelector } from "react-redux";

/**
 * يمنع الوصول لصفحات لوحة التحكم إلا لمستخدم بدور ADMIN.
 */
export default function AdminRoute({ children }) {
  const user = useSelector((state) => state.auth.user);

  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (user.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }
  return children;
}
