import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { configureStore } from "@reduxjs/toolkit";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import authReducer from "../features/auth/authSlice";
import cartReducer from "../features/cart/cartSlice";
import wishlistReducer from "../features/wishlist/wishlistSlice";
import uiReducer from "../features/ui/uiSlice";
import ProductCard from "../components/ProductCard";
import * as cartApi from "../api/cartApi";

jest.mock("../api/cartApi");

const product = {
  id: 1,
  name: "طبق Starlink V2",
  price: 900,
  discountPrice: null,
  imageUrl: "",
  category: "أطباق",
  averageRating: 4.5,
  stockQuantity: 10,
  lowStock: false,
  requiresShipping: true,
};

function renderWithProviders(ui) {
  const store = configureStore({
    reducer: { auth: authReducer, cart: cartReducer, wishlist: wishlistReducer, ui: uiReducer },
  });
  render(
    <Provider store={store}>
      <MemoryRouter>{ui}</MemoryRouter>
    </Provider>
  );
  return store;
}

describe("ProductCard", () => {
  test("الضغط على 'أضف للسلة' يستدعي الـ API ويحدّث السلة بالمتجر", async () => {
    cartApi.addCartItem.mockResolvedValue({
      id: 1,
      items: [{ id: 1, productId: 1, productName: product.name, unitPrice: 900, quantity: 1, subtotal: 900, availableStock: 10 }],
      totalAmount: 900,
    });

    const store = renderWithProviders(<ProductCard product={product} />);

    await userEvent.click(screen.getByRole("button", { name: "أضف للسلة" }));

    await waitFor(() => expect(cartApi.addCartItem).toHaveBeenCalledWith(1, 1));
    await waitFor(() => expect(store.getState().cart.totalAmount).toBe(900));
  });

  test("زر الإضافة للسلة يكون معطّلاً عند نفاد الكمية", () => {
    renderWithProviders(<ProductCard product={{ ...product, stockQuantity: 0 }} />);
    expect(screen.getByRole("button", { name: "نفذت الكمية" })).toBeDisabled();
  });
});
