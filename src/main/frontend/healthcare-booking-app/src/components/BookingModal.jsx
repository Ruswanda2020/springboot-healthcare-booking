// BookingModal.js
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import API_CONFIG from "../config/api.config";
import "../assets/css/BookingModal.css";

const BookingModal = ({ doctor, isOpen, onClose }) => {
  const navigate = useNavigate();

  const [selectedSpecialization, setSelectedSpecialization] = useState("");
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTime, setSelectedTime] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const timeSlots = [
    "07:00",
    "08:00",
    "09:00",
    "10:00",
    "11:00",
    "13:00",
    "14:00",
    "15:00",
    "16:00",
    "17:00",
  ];

  const handleBook = async () => {
    if (!selectedSpecialization || !selectedDate || !selectedTime) {
      setError("Please fill in all required fields");
      return;
    }
  
    setLoading(true);
    setError("");
  
    const endTime = new Date(`2000-01-01 ${selectedTime}`);
    endTime.setHours(endTime.getHours() + 1);
    const endTimeString = `${endTime.getHours().toString().padStart(2, "0")}:00`;
  
    const userData = JSON.parse(localStorage.getItem("userData"));
  
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.APPOINTMENTS}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
            "ngrok-skip-browser-warning": true,
          },
          body: JSON.stringify({
            user_id: userData.userId, // Sesuaikan dengan nama field yang benar
            doctor_id: doctor.id, // Sesuaikan dengan nama field yang benar
            doctor_specialization_id: parseInt(selectedSpecialization), // Sesuaikan dengan nama field yang benar
            appointment_date: selectedDate.toLocaleDateString("en-CA"), // Sesuaikan dengan nama field yang benar
            start_time: selectedTime, // Sesuaikan dengan nama field yang benar
            end_time: endTimeString, // Sesuaikan dengan nama field yang benar
          }),
        }
      );
  
      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || "Failed to book appointment");
      }
  
      navigate(`/appointments/${data.id}`, {
        state: { appointmentData: data },
      });
    } catch (error) {
      setError("An error occurred while booking the appointment");
    } finally {
      setLoading(false);
      onClose();
    }
  };
  

  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal-overlay">
    <div className="modal-content">
      <div className="modal-header">
        <h5 className="modal-title">Book Appointment</h5>
        <button type="button" className="close" onClick={onClose}>
          <span>&times;</span>
        </button>
      </div>
      <div className="modal-body">
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
          {loading ? "Booking..." : "Confirm Booking"}
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
