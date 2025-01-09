import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Badge, Button, Spinner } from "react-bootstrap";
import API_CONFIG from "../config/api.config";
import BookingModal from "./BookingModal";

const AppointmentList = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);
  const [showRescheduleModal, setShowRescheduleModal] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);

  useEffect(() => {
    const fetchAppointments = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await fetch(
          `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.APPOINTMENTS}`,
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${token}`,
              Accept: "application/json",
              "ngrok-skip-browser-warning": true,
            },
          }
        );

        if (!response.ok) {
          throw new Error("Failed to fetch appointments");
        }

        const data = await response.json();
        setAppointments(data);
      } catch (error) {
        console.error("Failed to fetch appointments", error);
        setError("Failed to fetch appointments");
      } finally {
        setLoading(false);
      }
    };
    fetchAppointments();
  }, []);

  const formatTime = (timeString) => {
    if (typeof timeString !== "string") {
      timeString = String(timeString);
    }
    return timeString.substring(0, 5);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };

  const handleReschedule = (appointment) => {
    setSelectedAppointment(appointment);
    setShowRescheduleModal(true);
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("id-ID", {
      style: "currency",
      currency: "IDR",
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case "PENDING":
        return <Badge bg="warning" text="dark">Pending</Badge>;
      case "CONFIRMED":
        return <Badge bg="success">Confirmed</Badge>;
      case "CANCELLED":
        return <Badge bg="danger">Cancelled</Badge>;
      default:
        return <Badge bg="secondary">Unknown</Badge>;
    }
  };

  const handleCancel = async (appointmentId) => {
    setCancellingId(appointmentId);
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.APPOINTMENTS}/cancel/${appointmentId}`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
            "ngrok-skip-browser-warning": true,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to cancel appointment");
      }

      setAppointments((prevAppointments) =>
        prevAppointments.filter((appointment) => appointment.id !== appointmentId)
      );
    } catch (error) {
      console.error("Failed to cancel appointment", error);
    } finally {
      setCancellingId(null);
    }
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center min-vh-100">
        <div className="text-center">
          <Spinner animation="border" variant="primary" />
          <p className="mt-4">Loading appointments...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="d-flex justify-content-center align-items-center min-vh-100">
        <div className="text-center">
          <p className="text-danger">{error}</p>
          <Button onClick={() => navigate('/home')} variant="link" className="mt-4">
            Return to Home
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-vh-100 py-4 d-flex justify-content-center align-items-center">
      <div className="container" style={{ maxWidth: "800px" }}>
        <div className="mb-4 text-center">
          <h1 className="display-4">My Appointments</h1>
        </div>

        <div className="bg-white shadow-sm rounded">
          {appointments.length === 0 ? (
            <div className="text-center py-5">
              <p className="text-muted">No appointments found</p>
            </div>
          ) : (
            <ul className="list-group list-group-flush">
              {appointments.map((appointment) => (
                <li key={appointment.id} className="list-group-item py-4">
                  <div className="d-flex justify-content-between align-items-start">
                    <div>
                      <p className="h5 mb-1">
                        {formatDate(appointment.appointment_date)}
                      </p>
                      <p className="text-muted">
                        {formatTime(appointment.start_time)} - {formatTime(appointment.end_time)}
                      </p>
                    </div>
                    <div className="d-flex align-items-center gap-2">
                      {appointment.status === "PENDING" && (
                        <>
                          <button
                            onClick={() => handleReschedule(appointment)}
                            className="btn btn-link text-primary p-0 me-2"
                          >
                            Reschedule
                          </button>
                          <Button
                            onClick={() => handleCancel(appointment.id)}
                            disabled={cancellingId === appointment.id}
                            variant="link"
                            className="text-danger p-0 me-2"
                          >
                            {cancellingId === appointment.id ? "Cancelling..." : "Cancel"}
                          </Button>
                        </>
                      )}
                      <Button
                        variant="link"
                        onClick={() => navigate(`/appointments/${appointment.id}`)}
                        className="text-primary p-0"
                      >
                        View Details →
                      </Button>
                    </div>
                  </div>

                  <div className="mt-2">
                    <span className="badge bg-info text-white me-2">
                      {appointment.consultation_type}
                    </span>
                    {getStatusBadgeClass(appointment.status)}
                  </div>

                  {appointment.payment_details && (
                    <div className="mt-2">
                      <span
                        className={`badge ${
                          appointment.payment_details.status === "SUCCESS"
                            ? "bg-success"
                            : appointment.payment_details.status === "CANCELLED"
                            ? "bg-warning"
                            : "bg-danger"
                        } text-white`}
                      >
                        Payment: {appointment.payment_details.status}
                      </span>
                    </div>
                  )}

                  {appointment.payment_details && (
                    <div className="text-muted mt-1">
                      Amount: {formatCurrency(appointment.payment_details.amount)}
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>

        {selectedAppointment && (
          <BookingModal
            isOpen={showRescheduleModal}
            onClose={() => {
              setShowRescheduleModal(false);
              setSelectedAppointment(null);
            }}
            mode="reschedule"
            appointmentId={selectedAppointment.id}
            initialDate={new Date(selectedAppointment.appointment_date)}
            initialStartTime={selectedAppointment.start_time}
          />
        )}

        <div className="text-center mt-4">
          <Button
            variant="link"
            onClick={() => navigate("/home")}
            className="text-primary"
          >
            Back to Home
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AppointmentList;
