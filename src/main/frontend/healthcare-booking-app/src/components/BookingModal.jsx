// BookingModal.js
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import API_CONFIG from "../config/api.config";
import "../assets/css/BookingModal.css";

const BookingModal = ({
  doctor,
  isOpen,
  onClose,
  mode = "booking",
  appointmentId = null,
  initialDate = null,
  initialTime = null,
}) => {
  const navigate = useNavigate();

  const [selectedSpecialization, setSelectedSpecialization] = useState("");
  const [selectedDate, setSelectedDate] = useState(() => (initialDate ? new Date(initialDate) : null));
  const [selectedTime, setSelectedTime] = useState(initialTime || "");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const timeSlots = ["07:00", "08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00"];

  const validateFields = () => {
    if (!selectedDate || !selectedTime || (mode === "booking" && !selectedSpecialization)) {
      setError("Please fill in all required fields");
      return false;
    }
    return true;
  };

  const handleBook = async () => {
    if (!validateFields()) return;

    setLoading(true);
    setError("");

    const endTime = new Date(`2000-01-01 ${selectedTime}`);
    endTime.setHours(endTime.getHours() + 1);
    const endTimeString = `${endTime.getHours().toString().padStart(2, "0")}:00`;

    const userData = JSON.parse(localStorage.getItem("userData"));
    const token = localStorage.getItem("token");

    const url =
      mode === "reschedule"
        ? `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.APPOINTMENTS}/reschedule/${appointmentId}`
        : `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.APPOINTMENTS}`;

    const method = mode === "reschedule" ? "PUT" : "POST";
    const body =
      mode === "reschedule"
        ? {
            appointment_date: selectedDate.toLocaleDateString("en-CA"),
            start_time: selectedTime,
            end_time: endTimeString,
          }
        : {
            user_id: userData.userId,
            doctor_id: doctor.id,
            doctor_specialization_id: parseInt(selectedSpecialization),
            appointment_date: selectedDate.toLocaleDateString("en-CA"),
            start_time: selectedTime,
            end_time: endTimeString,
          };

    try {
      const response = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
          "ngrok-skip-browser-warning": true,
        },
        body: JSON.stringify(body),
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || "Failed to process the request");
      }

      navigate(`/appointments/${mode === "reschedule" ? appointmentId : data.id}`, {
        state: {
          message: mode === "reschedule" ? "Appointment rescheduled successfully" : "Appointment booked successfully",
          type: "success",
        },
      });
    } catch (error) {
      setError(error.message || "An error occurred while processing the appointment");
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h5 className="modal-title">
            {mode === "reschedule" ? "Reschedule Appointment" : "Book Appointment"}
          </h5>
          <button type="button" className="close" onClick={onClose}>
            <span>&times;</span>
          </button>
        </div>
        <div className="modal-body">
          {mode !== "reschedule" && (
            <div className="form-group">
              <label>Select Specialization</label>
              <select
                value={selectedSpecialization}
                onChange={(e) => setSelectedSpecialization(e.target.value)}
                className="form-control"
              >
                <option value="">Select a specialization</option>
                {doctor.specializations.map((spec) => (
                  <option key={spec.specialization_id} value={spec.specialization_id}>
                    {spec.specialization_name} - {spec.consultation_type}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="form-group">
            <label>Select Date</label>
            <DatePicker
              selected={selectedDate}
              onChange={setSelectedDate}
              minDate={new Date()}
              className="form-control"
              placeholderText="Select date"
            />
          </div>

          <div className="form-group">
            <label>Select Time</label>
            <select
              value={selectedTime}
              onChange={(e) => setSelectedTime(e.target.value)}
              className="form-control"
            >
              <option value="">Select time slot</option>
              {timeSlots.map((time) => (
                <option key={time} value={time}>
                  {time}
                </option>
              ))}
            </select>
          </div>

          {error && (
            <div className="alert alert-danger" role="alert">
              {error}
            </div>
          )}
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleBook}
            disabled={loading}
          >
            {loading ? "Processing..." : mode === "reschedule" ? "Confirm Reschedule" : "Confirm Booking"}
          </button>
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default BookingModal;
