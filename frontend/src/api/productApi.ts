import axios from "axios";
import type { Product } from "../types/product";
const API_URL = "http://localhost:8081/api/products";
export const getProducts = async (): Promise<Product[]> => {
    const response =
        await axios.get<Product[]>(
            API_URL
        );
    return response.data.filter(
        (product) =>
            typeof product.productName === "string" &&
            product.productName.trim().length > 0
    );
};
export const addProduct = async (
    product: Omit<Product, "productId">
): Promise<Product> => {
    const response =
        await axios.post<Product>(
            API_URL,
            product
        );
    return response.data;
};
