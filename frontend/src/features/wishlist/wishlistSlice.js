import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import * as wishlistApi from "../../api/wishlistApi";

/**
 * تجلب قائمة رغبات المستخدم الحالي.
 */
export const loadWishlist = createAsyncThunk("wishlist/load", async () => wishlistApi.fetchWishlist());

/**
 * تضيف منتجاً لقائمة الرغبات ثم تعيد تحميل القائمة.
 */
export const addWishlistItem = createAsyncThunk("wishlist/add", async (productId, { dispatch }) => {
  await wishlistApi.addToWishlist(productId);
  return dispatch(loadWishlist()).unwrap();
});

/**
 * تحذف منتجاً من قائمة الرغبات ثم تعيد تحميل القائمة.
 */
export const removeWishlistItem = createAsyncThunk("wishlist/remove", async (productId, { dispatch }) => {
  await wishlistApi.removeFromWishlist(productId);
  return dispatch(loadWishlist()).unwrap();
});

const initialState = {
  items: [],
  status: "idle",
};

const wishlistSlice = createSlice({
  name: "wishlist",
  initialState,
  reducers: {
    clearWishlistState(state) {
      state.items = [];
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loadWishlist.pending, (state) => {
        state.status = "loading";
      })
      .addCase(loadWishlist.fulfilled, (state, action) => {
        state.items = action.payload;
        state.status = "succeeded";
      })
      .addCase(loadWishlist.rejected, (state) => {
        state.status = "failed";
      });
  },
});

export const { clearWishlistState } = wishlistSlice.actions;

export const selectIsInWishlist = (productId) => (state) =>
  state.wishlist.items.some((item) => item.product.id === productId);

export default wishlistSlice.reducer;
