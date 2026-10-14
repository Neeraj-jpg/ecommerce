import {
    useForm
} from "react-hook-form";

import {
    zodResolver
} from "@hookform/resolvers/zod";

import {
    checkoutSchema,
    type CheckoutFormData
} from "../schemas/checkoutSchema";

import {
    useCartStore
} from "../store/cartStore";
import {
    useAuthStore
} from "../store/authStore";

declare global {
    interface Window {
        Razorpay: any;
    }
}

function CheckoutForm() {

    const getTotal = useCartStore(
        (state) => state.getTotal
    );

    const total = getTotal();
    const cartItems = useCartStore(
        (state) => state.cartItems
    );
    const clearCart = useCartStore(
        (state) => state.clearCart
    );
    const user = useAuthStore(
        (state) => state.user
    );

    const {
        register,
        handleSubmit,
        formState: {
            errors
        }
    } = useForm<CheckoutFormData>({
        resolver: zodResolver(
            checkoutSchema
        ),
        defaultValues: {
            email: user?.email ?? ""
        }
    });

    const onSubmit = async (
        data: CheckoutFormData
    ) => {

        if (!user?.token || cartItems.length === 0) {
            alert("Sign in and add items to your cart before checkout.");
            return;
        }

        try {

            const response = await fetch(
                "http://localhost:8081/api/payment/create-order",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${user.token}`
                    },
                    body: JSON.stringify({
                        amount: total,
                        receipt: "ORDER_" + Date.now(),
                        items: cartItems.map((item) => ({
                            productId: item.product.productId,
                            quantity: item.quantity
                        }))
                    })
                }
            );

            if (!response.ok) {

                const errorData =
                    await response.json();

                throw new Error(
                    errorData.error ||
                    "Failed to create Razorpay order"
                );
            }

            const order =
                await response.json();

            const options = {

                key: "rzp_test_TiA637G49WmEYH",

                amount: order.amount,

                currency: order.currency,

                name: "SmartCart",

                description:
                    "E-Commerce Payment",

                order_id: order.id,

                prefill: {
                    name: data.name,
                    email: data.email,
                    contact: data.phone
                },

                notes: {
                    address: data.address
                },

                theme: {
                    color: "#3399cc"
                },

                handler: async function (
                    razorpayResponse: any
                ) {

                    try {

                        const verifyResponse =
                            await fetch(
                                "http://localhost:8081/api/payment/verify",
                                {
                                    method: "POST",

                                    headers: {
                                        "Content-Type":
                                            "application/json",
                                        "Authorization":
                                            `Bearer ${user.token}`
                                    },

                                    body: JSON.stringify({

                                        razorpayOrderId:
                                            razorpayResponse
                                                .razorpay_order_id,

                                        razorpayPaymentId:
                                            razorpayResponse
                                                .razorpay_payment_id,

                                        razorpaySignature:
                                            razorpayResponse
                                                .razorpay_signature
                                    })
                                }
                            );

                        const verifyData =
                            await verifyResponse.json();

                        if (!verifyResponse.ok) {

                            throw new Error(
                                verifyData.message ||
                                verifyData.error ||
                                "Payment verification failed"
                            );
                        }

                        if (verifyData.success) {

                            clearCart();

                            alert(
                                "Payment successful!\n\n" +
                                "Payment ID: " +
                                razorpayResponse
                                    .razorpay_payment_id
                            );

                        } else {

                            alert(
                                "Payment verification failed."
                            );
                        }

                    } catch (error) {

                        console.error(
                            "Verification error:",
                            error
                        );

                        alert(
                            "Payment was completed, " +
                            "but verification failed."
                        );
                    }
                },

                modal: {

                    ondismiss: function () {

                        alert(
                            "Payment cancelled."
                        );
                    }
                }
            };

            const razorpay =
                new window.Razorpay(options);

            razorpay.open();

        } catch (error) {

            console.error(error);

            alert(
                "Payment Error:\n\n" +
                (
                    error instanceof Error
                        ? error.message
                        : "Unknown error"
                )
            );
        }
    };

    return (
        <div className="checkout">

            <h2>
                Checkout
            </h2>

            <form
                onSubmit={
                    handleSubmit(onSubmit)
                }
            >

                <div>
                    <label>
                        Name
                    </label>

                    <input
                        type="text"
                        {...register("name")}
                    />

                    {errors.name && (
                        <p className="error-message">
                            {errors.name.message}
                        </p>
                    )}
                </div>

                <div>
                    <label>
                        Email
                    </label>

                    <input
                        type="email"
                        readOnly
                        {...register("email")}
                    />

                    {errors.email && (
                        <p className="error-message">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                <div>
                    <label>
                        Phone
                    </label>

                    <input
                        type="tel"
                        {...register("phone")}
                    />

                    {errors.phone && (
                        <p className="error-message">
                            {errors.phone.message}
                        </p>
                    )}
                </div>

                <div>
                    <label>
                        Address
                    </label>

                    <textarea
                        {...register("address")}
                    />

                    {errors.address && (
                        <p className="error-message">
                            {errors.address.message}
                        </p>
                    )}
                </div>

                <h3>
                    Order Total: ₹{total}
                </h3>

                <button type="submit">
                    Place Order & Pay
                </button>

            </form>

        </div>
    );
}

export default CheckoutForm;