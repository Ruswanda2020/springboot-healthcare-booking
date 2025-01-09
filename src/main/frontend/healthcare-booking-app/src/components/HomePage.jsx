import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Navbar, Nav, Container, Button, Form, InputGroup, Modal, Row, Col } from "react-bootstrap";
import DoctorCard from "./DoctorCard";
import API_CONFIG from "../config/api.config";

const HomePage = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [userData, setUserData] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    size: 10,
  });

  // Fetch user data from localStorage
  useEffect(() => {
    try {
      const storedUserData = localStorage.getItem("userData");
      if (storedUserData) {
        setUserData(JSON.parse(storedUserData));
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
    setShowLogoutModal(false);
    navigate("/login");
  };

  const searchDoctors = async (page = 0) => {
    setLoading(true);
    setError("");
    const sort = ["name,asc"];
    const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DOCTORS}?keyword=${searchQuery}&page=${page}&size=${pagination.size}&sort=${encodeURIComponent(sort.join(","))}`;
    console.log("Fetching data from:", url);  // Debugging the URL
    
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: "application/json",
         'ngrok-skip-browser-warning': true,
        },
      });
  
      
      // Log the response headers and body for debugging
      // console.log("Response headers:", response.headers);
      // const text = await response.text();
      // console.log("Response body:", text);
    
      if (response.ok) {
        const data = await response.json(); // Parse as JSON
        setDoctors(data.content);
        setPagination({
          currentPage: data.number,
          totalPages: data.totalPages,
          totalElements: data.totalElements,
          size: data.size,
        });
      } else {
        throw new Error('Failed to fetch data');
      }
    } catch (err) {
      setError(err.message || "Failed to connect to the server");
    } finally {
      setLoading(false);
    }
  };
  
  
  

  const handleSearch = (e) => {
    e.preventDefault();
    searchDoctors(0);
  };

  const handlePageChange = (newPage) => {
    searchDoctors(newPage);
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
              onClick={() => setShowLogoutModal(true)}
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

      {/* Doctor Cards Section */}
      <div className="mt-4">
        {loading && (
          <div className="text-center py-4">
            <span className="text-secondary">Loading...</span>
          </div>
        )}
        {error && (
          <div className="text-center py-4">
            <span className="text-danger">{error}</span>
          </div>
        )}
        {!loading && !error && doctors.length === 0 && searchQuery && (
          <div className="text-center py-4">
            <span className="text-muted">No doctors found</span>
          </div>
        )}
        {!loading && !error && doctors.length > 0 && (
          <Container>
            <Row className="g-3">
              {doctors.map((doctor) => (
                <Col key={doctor.id} xs={12} md={6} lg={4}>
                  <DoctorCard doctor={doctor} />
                </Col>
              ))}
            </Row>
          </Container>
        )}
        {pagination.totalPages > 1 && (
          <div className="d-flex justify-content-center align-items-center mt-4">
            <Button
              onClick={() => handlePageChange(pagination.currentPage - 1)}
              disabled={pagination.currentPage === 0}
              variant="outline-secondary"
              className="me-2"
            >
              Previous
            </Button>
            <span className="text-muted">
              Page {pagination.currentPage + 1} of {pagination.totalPages}
            </span>
            <Button
              onClick={() => handlePageChange(pagination.currentPage + 1)}
              disabled={pagination.currentPage === pagination.totalPages - 1}
              variant="outline-secondary"
              className="ms-2"
            >
              Next
            </Button>
          </div>
        )}
      </div>

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
