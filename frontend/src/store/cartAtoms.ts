

import { atom } from "jotai";
import type { CartItem } from "../types/cart";
export const cartItemsAtom = atom<CartItem[]>([]);
