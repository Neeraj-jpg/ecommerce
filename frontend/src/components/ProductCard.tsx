import type { Product } from "../types/product";
import { useCartStore } from "../store/cartStore";
interface ProductCardProps {
    product: Product;
}

function ProductCard({ product }: ProductCardProps) {

    const addToCart = useCartStore(
        (state) => state.addToCart
    );

    return (

        <div className="product-card">

            <img
                src={product.imageUrl}
                alt={product.productName}
            />

            <h3>{product.productName}</h3>

            <p>Category: {product.category}</p>

            <p>₹{product.price}</p>

            <p>
                Rating: ⭐ {product.rating}
            </p>

            <p>
                {product.stock > 0
                    ? `In Stock (${product.stock})`
                    : "Out of Stock"}
            </p>

            <button
                onClick={() => addToCart(product)}
                disabled={product.stock === 0}
            >
                {product.stock > 0
                    ? "Add to Cart"
                    : "Out of Stock"}
            </button>

        </div>
    );
}

export default ProductCard;
