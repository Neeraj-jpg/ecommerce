import {
    useState,
    useEffect
} from "react";

import Navbar
    from "./components/Navbar";

import ProductList
    from "./components/ProductList";

import SignupPage
    from "./pages/SignupPage";

import SigninPage
    from "./pages/SigninPage";

import AdminDashboard
    from "./pages/AdminDashboard";

import UserDashboard
    from "./pages/UserDashboard";

import "./App.css";

function App() {

    const [
        page,
        setPage
    ] = useState("products");

    const [
        opening,
        setOpening
    ] = useState(true);

    useEffect(() => {

        const timer = setTimeout(() => {
            setOpening(false);
        }, 5000);

        return () => clearTimeout(timer);

    }, []);

    const renderPage = () => {

        switch (page) {

            case "signup":
                return (
                    <SignupPage />
                );

            case "signin":
                return (
                    <SigninPage
                        setPage={
                            setPage
                        }
                    />
                );

            case "admin":
                return (
                    <AdminDashboard />
                );

            case "user":
                return (
                    <UserDashboard />
                );

            case "products":
            default:
                return (
                    <>
                        <h1>
                            E-Commerce
                            Product Management System
                        </h1>

                        <p className="subtitle">
                            Browse Products
                        </p>

                        <ProductList />
                    </>
                );
        }
    };

    return (
        <>
            {opening && (

                <div className="temple-opening">

                    <div className="temple-sky">

                        <div className="moon">
                            🌕
                        </div>

                        <div className="stars">
                            ✦　✧　✦　　✧　✦
                            <br />
                            ✧　✦　　✧　　✦
                        </div>

                        <div className="hills">

                            <div className="hill hill1"></div>
                            <div className="hill hill2"></div>
                            <div className="hill hill3"></div>
                            <div className="hill hill4"></div>
                            <div className="hill hill5"></div>
                            <div className="hill hill6"></div>
                            <div className="hill hill7"></div>

                        </div>

                        <div className="temple-gopuram">

                            <div className="gopuram-symbol">
                                🛕
                            </div>

                            <div className="om-symbol">
                                ॐ
                            </div>

                        </div>

                        <div className="welcome-text">

                            <div className="om-large">
                                ॐ
                            </div>

                            <div>
                                WELCOME
                            </div>

                        </div>

                        <div className="temple-door left-door">

                            <div className="door-om">
                                ॐ
                            </div>

                        </div>

                        <div className="temple-door right-door">

                            <div className="door-om">
                                ॐ
                            </div>

                        </div>

                    </div>

                </div>
            )}

            <div
                className={
                    opening
                        ? "main-website website-hidden"
                        : "main-website website-visible"
                }
            >

                <Navbar
                    setPage={
                        setPage
                    }
                />

                <div className="container">

                    {renderPage()}

                </div>

            </div>
        </>
    );
}

export default App;