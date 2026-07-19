import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { configureStore } from "@reduxjs/toolkit";
import { Provider } from "react-redux";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import authReducer from "../features/auth/authSlice";
import cartReducer from "../features/cart/cartSlice";
import wishlistReducer from "../features/wishlist/wishlistSlice";
import uiReducer from "../features/ui/uiSlice";
import ProductDetailPage from "../pages/ProductDetailPage";
import * as productsApi from "../api/productsApi";

jest.mock("../api/productsApi");

const miniProduct = {
  id: 1,
  name: "طبق Starlink Mini",
  description: "وصف",
  price: 500,
  discountPrice: null,
  imageUrl: "",
  category: "أطباق",
  averageRating: 4,
  stockQuantity: 10,
  lowStock: false,
  requiresShipping: true,
  variantGroupKey: "starlink-dish",
  variantLabel: "Mini",
};

const standardVariant = { id: 2, variantLabel: "Standard", price: 900, discountPrice: null, stockQuantity: 5, isActive: true };
const miniVariant = { id: 1, variantLabel: "Mini", price: 500, discountPrice: null, stockQuantity: 10, isActive: true };

function renderPage(productId = "1") {
  const store = configureStore({
    reducer: { auth: authReducer, cart: cartReducer, wishlist: wishlistReducer, ui: uiReducer },
  });
  render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[`/products/${productId}`]}>
        <Routes>
          <Route path="/products/:id" element={<ProductDetailPage />} />
        </Routes>
      </MemoryRouter>
    </Provider>
  );
}

describe("ProductDetailPage - موديلات المنتج (Variants)", () => {
  beforeEach(() => {
    productsApi.fetchRelatedProducts.mockResolvedValue([]);
    productsApi.fetchProductReviews.mockResolvedValue({ content: [] });
  });

  test("لا يعرض Dropdown الموديلات عندما يكون للمنتج موديل واحد فقط", async () => {
    productsApi.fetchProduct.mockResolvedValue(miniProduct);
    productsApi.fetchProductVariants.mockResolvedValue([]);

    renderPage("1");

    await screen.findByRole("heading", { name: "طبق Starlink Mini" });
    expect(screen.queryByLabelText("الموديل")).not.toBeInTheDocument();
  });

  test("يعرض Dropdown الموديلات عند وجود أكثر من موديل بنفس المجموعة", async () => {
    productsApi.fetchProduct.mockResolvedValue(miniProduct);
    productsApi.fetchProductVariants.mockResolvedValue([miniVariant, standardVariant]);

    renderPage("1");

    const select = await screen.findByLabelText("الموديل");
    expect(select.value).toBe("1");
    expect(screen.getByRole("option", { name: "Mini" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "Standard" })).toBeInTheDocument();
  });

  test("اختيار موديل آخر من القائمة ينتقل لصفحة ذلك الموديل", async () => {
    productsApi.fetchProduct.mockResolvedValue(miniProduct);
    productsApi.fetchProductVariants.mockResolvedValue([miniVariant, standardVariant]);

    renderPage("1");

    const select = await screen.findByLabelText("الموديل");
    await userEvent.selectOptions(select, "2");

    await waitFor(() => expect(productsApi.fetchProduct).toHaveBeenCalledWith("2"));
  });
});
