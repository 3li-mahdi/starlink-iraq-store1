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
import * as productsApi from "../api/productsApi";

jest.mock("../api/cartApi");
jest.mock("../api/productsApi");
jest.mock("../components/three/ProductThumbnail3D", () => ({
  __esModule: true,
  default: () => <div data-testid="thumbnail-3d-mock" />,
}));
jest.mock("../components/three/Product3DViewer", () => ({
  __esModule: true,
  default: () => <div data-testid="viewer-3d-mock" />,
}));

const product = {
  id: 1,
  name: "طبق Starlink V2",
  description: "طبق استقبال أصلي بجودة عالية",
  price: 900,
  discountPrice: null,
  imageUrl: "",
  category: "أطباق ومعدات",
  productType: "PHYSICAL",
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

describe("ProductQuickViewModal (عبر الضغط على بطاقة المنتج)", () => {
  beforeEach(() => {
    productsApi.fetchProductVariants.mockResolvedValue([]);
  });

  test("الضغط العادي على بطاقة المنتج يفتح مودال المعاينة السريعة بدل الانتقال المباشر", async () => {
    renderWithProviders(<ProductCard product={product} />);

    await userEvent.click(screen.getByRole("heading", { name: "طبق Starlink V2" }));

    const dialog = await screen.findByRole("dialog");
    expect(dialog).toBeInTheDocument();
    expect(screen.getByTestId("viewer-3d-mock")).toBeInTheDocument();
    expect(screen.getByText("طبق استقبال أصلي بجودة عالية")).toBeInTheDocument();
  });

  test("إضافة المنتج للسلة من داخل المودال تستدعي الـ API الحقيقي وتُغلق المودال", async () => {
    cartApi.addCartItem.mockResolvedValue({
      id: 1,
      items: [{ id: 1, productId: 1, productName: product.name, unitPrice: 900, quantity: 1, subtotal: 900, availableStock: 10 }],
      totalAmount: 900,
    });

    const store = renderWithProviders(<ProductCard product={product} />);
    await userEvent.click(screen.getByRole("heading", { name: "طبق Starlink V2" }));
    await screen.findByRole("dialog");

    const addButtons = screen.getAllByRole("button", { name: "أضف للسلة" });
    await userEvent.click(addButtons[addButtons.length - 1]);

    await waitFor(() => expect(cartApi.addCartItem).toHaveBeenCalledWith(1, 1));
    await waitFor(() => expect(store.getState().cart.totalAmount).toBe(900));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });
});
