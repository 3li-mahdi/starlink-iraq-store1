import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import * as cartApi from "../../api/cartApi";

/**
 * تجلب سلة التسوق الحالية من الـ API وتحدّث المتجر.
 */
export const loadCart = createAsyncThunk("cart/load", async () => cartApi.fetchCart());

/**
 * تضيف منتجاً للسلة (تحديث فوري للسعر الإجمالي - optimistic بعد استجابة السيرفر مباشرة).
 */
export const addItemToCart = createAsyncThunk(
  "cart/addItem",
  async ({ productId, quantity }, { rejectWithValue }) => {
    try {
      return await cartApi.addCartItem(productId, quantity);
    } catch (error) {
      return rejectWithValue(error.response?.data?.error || "تعذّرت إضافة المنتج للسلة");
    }
  }
);

/**
 * تحدّث كمية عنصر بالسلة.
 */
export const updateCartItemQuantity = createAsyncThunk(
  "cart/updateItem",
  async ({ itemId, quantity }, { rejectWithValue }) => {
    try {
      return await cartApi.updateCartItem(itemId, quantity);
    } catch (error) {
      return rejectWithValue(error.response?.data?.error || "تعذّر تحديث الكمية");
    }
  }
);

/**
 * تحذف عنصراً من السلة.
 */
export const removeItemFromCart = createAsyncThunk("cart/removeItem", async (itemId) =>
  cartApi.removeCartItem(itemId)
);

const initialState = {
  id: null,
  items: [],
  totalAmount: 0,
  status: "idle",
  error: null,
};

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {
    clearCartState(state) {
      state.id = null;
      state.items = [];
      state.totalAmount = 0;
    },
  },
  extraReducers: (builder) => {
    const applyCart = (state, action) => {
      state.id = action.payload.id;
      state.items = action.payload.items;
      state.totalAmount = action.payload.totalAmount;
      state.status = "succeeded";
      state.error = null;
    };

    builder
      .addCase(loadCart.pending, (state) => {
        state.status = "loading";
      })
      .addCase(loadCart.fulfilled, applyCart)
      .addCase(loadCart.rejected, (state) => {
        state.status = "failed";
      })
      .addCase(addItemToCart.fulfilled, applyCart)
      .addCase(addItemToCart.rejected, (state, action) => {
        state.error = action.payload;
      })
      .addCase(updateCartItemQuantity.fulfilled, applyCart)
      .addCase(updateCartItemQuantity.rejected, (state, action) => {
        state.error = action.payload;
      })
      .addCase(removeItemFromCart.fulfilled, applyCart);
  },
});

export const { clearCartState } = cartSlice.actions;

export const selectCartItemCount = (state) => state.cart.items.reduce((sum, item) => sum + item.quantity, 0);

export default cartSlice.reducer;
