import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Navbar, Nav, Container, Button, Form, InputGroup, Modal } from "react-bootstrap";

const HomePage = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [userData, setUserData] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  // Mengambil data user dari localStorage
  useEffect(() => {
    try {
      const storedUserData = localStorage.getItem("userData");
      if (storedUserData) {
        const parsedData = JSON.parse(storedUserData);
        setUserData(parsedData);
      } else {
        console.warn("No userData found in localStorage.");
      }
    } catch (error) {
      console.error("Error parsing userData from localStorage:", error);
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userData");
    setShowLogoutModal(false); // Tutup modal
    navigate("/login"); // Arahkan ke halaman login
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      alert("Please enter a search query.");
      return;
    }
    // TODO: Tambahkan logika pencarian di sini
    console.log("Searching for:", searchQuery);
  };

  return (
    <div className="vh-100 d-flex flex-column">
      {/* Navigation Bar */}
      <Navbar bg="light" expand="lg" className="shadow-sm">
        <Container>
          <Navbar.Brand className="fw-bold">Healthcare System</Navbar.Brand>
          <Nav className="ms-auto">
            <Nav.Item className="me-3">
              {userData ? (
                <span className="text-muted">Welcome, {userData.username}</span>
              ) : (
                <span className="text-muted">Welcome, Guest</span>
              )}
            </Nav.Item>
            <Button
              variant="outline-danger"
              size="sm"
              onClick={() => setShowLogoutModal(true)} // Tampilkan modal saat klik logout
            >
              Logout
            </Button>
          </Nav>
        </Container>
      </Navbar>

      {/* Search Section */}
      <Container className="flex-grow-1 d-flex flex-column justify-content-center align-items-center">
        <h2 className="mb-4 fw-bold text-center">Find a Doctor</h2>
        <Form onSubmit={handleSearch} className="w-100" style={{ maxWidth: "500px" }}>
          <InputGroup className="mb-3">
            <Form.Control
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search for doctors"
            />
            <Button type="submit" variant="primary">
              Search
            </Button>
          </InputGroup>
        </Form>
      </Container>

      {/* Logout Modal */}
      <Modal show={showLogoutModal} onHide={() => setShowLogoutModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Confirm Logout</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Are you sure you want to log out?</p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowLogoutModal(false)}>
            Cancel
          </Button>
          <Button variant="danger" onClick={handleLogout}>
            Logout
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default HomePage;
