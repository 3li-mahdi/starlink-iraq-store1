import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { configureStore } from "@reduxjs/toolkit";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import authReducer from "../features/auth/authSlice";
import cartReducer from "../features/cart/cartSlice";
import wishlistReducer from "../features/wishlist/wishlistSlice";
import uiReducer, { openCartDrawer } from "../features/ui/uiSlice";
import CartDrawer from "../components/CartDrawer";
import * as cartApi from "../api/cartApi";

jest.mock("../api/cartApi");

const sampleCart = {
  id: 1,
  items: [{ id: 1, productId: 5, productName: "طبق Starlink", unitPrice: 900, quantity: 2, subtotal: 1800, availableStock: 10 }],
  totalAmount: 1800,
};

function renderWithProviders({ authState } = {}) {
  const store = configureStore({
    reducer: { auth: authReducer, cart: cartReducer, wishlist: wishlistReducer, ui: uiReducer },
    preloadedState: {
      auth: { user: authState ?? null, accessToken: null, status: "idle", error: null },
    },
  });
  render(
    <Provider store={store}>
      <MemoryRouter>
        <CartDrawer />
      </MemoryRouter>
    </Provider>
  );
  return store;
}

describe("CartDrawer", () => {
  test("لا تظهر أي عناصر بالسلة الجانبية عندما تكون مغلقة", () => {
    renderWithProviders();
    expect(screen.queryByRole("dialog", { name: "سلة التسوق" })).not.toBeInTheDocument();
  });

  test("تعرض حالة فارغة عندما تكون السلة فارغة", async () => {
    cartApi.fetchCart.mockResolvedValue({ id: 1, items: [], totalAmount: 0 });
    const store = renderWithProviders();
    act(() => store.dispatch(openCartDrawer()));

    await screen.findByText("سلتك فارغة");
  });

  test("الضغط على 'إتمام الشراء' بدون تسجيل دخول يفتح مودال الدخول بدل الانتقال المباشر", async () => {
    cartApi.fetchCart.mockResolvedValue(sampleCart);
    const store = renderWithProviders();
    act(() => store.dispatch(openCartDrawer()));

    const checkoutButton = await screen.findByRole("button", { name: "إتمام الشراء" });
    await userEvent.click(checkoutButton);

    await waitFor(() => {
      expect(store.getState().ui.isAuthModalOpen).toBe(true);
      expect(store.getState().ui.authModalRedirectTo).toBe("/checkout");
    });
    expect(store.getState().ui.isCartDrawerOpen).toBe(false);
  });

  test("تعرض السعر الإجمالي الحقيقي القادم من الـ Backend وليس بيانات محلية", async () => {
    cartApi.fetchCart.mockResolvedValue(sampleCart);
    const store = renderWithProviders();
    act(() => store.dispatch(openCartDrawer()));

    await screen.findByText("طبق Starlink");
    expect(screen.getByText(/1,800/)).toBeInTheDocument();
  });
});
