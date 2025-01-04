import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Form, Button, Container, Alert, Spinner } from "react-bootstrap";

const RegisterPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    password_confirmation: "",
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    // Validasi sederhana
    if (formData.password !== formData.password_confirmation) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/v1/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "*/*",
        },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Register failed");
      }

      navigate("/login", {
        state: { 
          message: "Registration successful, please login with your credentials", 
          type: "success",
        },
      });
    } catch (err) {
      console.error("Register Error:", err); // Log error for debug
      setError(err.message || "Failed to connect to the server");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="d-flex flex-column align-items-center justify-content-center vh-100">
      <div className="w-100" style={{ maxWidth: "400px" }}>
        <h2 className="text-center mb-4">Create your account</h2>

        {error && (
          <Alert variant="danger" className="text-center">
            {error}
          </Alert>
        )}

        <Form onSubmit={handleSubmit}>
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

          <Form.Group className="mb-3" controlId="email">
            <Form.Label>Email</Form.Label>
            <Form.Control
              type="email"
              placeholder="Enter email"
              value={formData.email}
              onChange={(e) =>
                setFormData({ ...formData, email: e.target.value })
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

          <Form.Group className="mb-3" controlId="password_confirmation">
            <Form.Label>Confirm Password</Form.Label>
            <Form.Control
              type="password"
              placeholder="Confirm password"
              value={formData.password_confirmation}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  password_confirmation: e.target.value,
                })
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
                  Registering...
                </>
              ) : (
                "Register"
              )}
            </Button>

            <Button
              variant="outline-primary"
              onClick={() => navigate('/login')}
            >
                Already have an account? Sign in
            </Button>
          </div>
        </Form>
      </div>
    </Container>
  );
};

export default RegisterPage;
