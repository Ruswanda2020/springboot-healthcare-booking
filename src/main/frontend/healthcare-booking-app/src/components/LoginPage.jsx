import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Form, Button, Container, Alert, Spinner } from "react-bootstrap";

const LoginPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [formData, setFormData] = useState({
        username: "",
        password: "",
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    // Get success message from registration if any
    const successMessage = location.state?.message;
    const errorMessage = location.state?.error;

    const handleSubmission = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(""); // Reset error state

        try {
            const response = await fetch("http://localhost:8080/api/v1/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "*/*",
                },
                body: JSON.stringify(formData),
                credentials: "include",
            });

            const data = await response.json();
            console.log("Login response data:", data);

            if (!response.ok) {
                throw new Error(data.message || "Login failed");
            }

            // Store token and user data
            localStorage.setItem("token", data.token);
            localStorage.setItem(
                "userData",
                JSON.stringify({
                    userId: data.user_id,
                    username: data.user_name, // Pastikan sesuai dengan response API
                    email: data.email,
                    roles: data.roles,
                })
            );

            // Debug log to check data stored in localStorage
            console.log("Stored userData in localStorage:", localStorage.getItem("userData"));

            navigate("/home");
        } catch (err) {
            console.error("Login error:", err); // Log error for debug
            setError(err.message || "Failed to connect to the server");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="d-flex flex-column align-items-center justify-content-center vh-100">
            <div className="w-100" style={{ maxWidth: "400px" }}>
                <h2 className="text-center mb-4">Sign in to your account</h2>

                {successMessage && (
                    <Alert variant="success" className="text-center">
                        {successMessage}
                    </Alert>
                )}
                {errorMessage && (
                    <Alert variant="danger" className="text-center">
                        {errorMessage}
                    </Alert>
                )}

                <Form onSubmit={handleSubmission}>
                    <Form.Group className="mb-3" controlId="username">
                        <Form.Label>Username</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter username"
                            value={formData.username}
                            onChange={(e) =>
                                setFormData({ ...formData, username: e.target.value })
                            }
                            required
                        />
                    </Form.Group>

                    <Form.Group className="mb-3" controlId="password">
                        <Form.Label>Password</Form.Label>
                        <Form.Control
                            type="password"
                            placeholder="Enter password"
                            value={formData.password}
                            onChange={(e) =>
                                setFormData({ ...formData, password: e.target.value })
                            }
                            required
                        />
                    </Form.Group>

                    {error && <Alert variant="danger">{error}</Alert>}

                    <div className="d-flex flex-column">
                        <Button variant="primary" type="submit" className="mb-2" disabled={loading}>
                            {loading ? (
                                <>
                                    <Spinner
                                        as="span"
                                        animation="border"
                                        size="sm"
                                        role="status"
                                        aria-hidden="true"
                                    />{" "}
                                    Signing in...
                                </>
                            ) : (
                                "Sign in"
                            )}
                        </Button>

                        <Button
                            variant="outline-primary"
                            onClick={() => navigate('/register')}
                        >
                            Create new account
                        </Button>
                    </div>
                </Form>
            </div>
        </Container>
    );
};

export default LoginPage;
