import { Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import Footer from "./components/Footer";
import ToastContainer from "./components/ToastContainer";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminRoute from "./components/AdminRoute";
import HomePage from "./pages/HomePage";
import ProductListPage from "./pages/ProductListPage";
import ProductDetailPage from "./pages/ProductDetailPage";
import CartPage from "./pages/CartPage";
import WishlistPage from "./pages/WishlistPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import CheckoutPage from "./pages/CheckoutPage";
import OrderConfirmationPage from "./pages/OrderConfirmationPage";
import OrdersPage from "./pages/OrdersPage";
import NotFoundPage from "./pages/NotFoundPage";
import AdminLayout from "./pages/admin/AdminLayout";
import AdminProductsPage from "./pages/admin/AdminProductsPage";
import AdminOrdersPage from "./pages/admin/AdminOrdersPage";
import AdminCouponsPage from "./pages/admin/AdminCouponsPage";
import ContactPage from "./pages/ContactPage";
import HelpCenterPage from "./pages/HelpCenterPage";
import useSessionBootstrap from "./hooks/useSessionBootstrap";

/**
 * جذر التطبيق: يضبط شريط التنقل، التذييل، الإشعارات، وكل مسارات الصفحات.
 */
export default function App() {
  useSessionBootstrap();

  return (
    <div style={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <Navbar />
      <ToastContainer />
      <main style={{ flex: 1 }} className="container">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/contact" element={<ContactPage />} />
          <Route path="/help" element={<HelpCenterPage />} />
          <Route
            path="/wishlist"
            element={
              <ProtectedRoute>
                <WishlistPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/checkout"
            element={
              <ProtectedRoute>
                <CheckoutPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/orders"
            element={
              <ProtectedRoute>
                <OrdersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/orders/:id"
            element={
              <ProtectedRoute>
                <OrderConfirmationPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdminLayout />
              </AdminRoute>
            }
          >
            <Route index element={<AdminProductsPage />} />
            <Route path="orders" element={<AdminOrdersPage />} />
            <Route path="coupons" element={<AdminCouponsPage />} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
