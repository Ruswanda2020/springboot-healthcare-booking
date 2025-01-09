import React, { useState } from "react";
import BookingModal from "./BookingModal";
import { Card, Button, Badge, ListGroup } from "react-bootstrap";

const formatRupiah = (amount) => {
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    minimumFractionDigits: 0,
  }).format(amount);
};

const DoctorCard = ({ doctor }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <Card className="shadow-sm mb-4">
      <Card.Body>
        <Card.Title className="text-dark">{doctor.name}</Card.Title>
        <Card.Subtitle className="mb-3 text-muted">{doctor.hospital_name}</Card.Subtitle>
        <Card.Text className="text-secondary">{doctor.bio}</Card.Text>

        <h6 className="mt-4">Specializations</h6>
        <div className="d-flex flex-wrap gap-2">
          {doctor.specializations.map((spec, index) => (
            <Badge bg="info" key={index} className="text-dark">
              {spec.specialization_name}
            </Badge>
          ))}
        </div>
      </Card.Body>

      <ListGroup variant="flush">
        <ListGroup.Item>
          <h6>Consultation Types</h6>
        </ListGroup.Item>
        {doctor.specializations.map((spec, index) => (
          <ListGroup.Item
            key={index}
            className="d-flex justify-content-between align-items-center"
          >
            <span className="text-muted">{spec.consultation_type}</span>
            <span className="fw-bold">{formatRupiah(spec.base_fee)}/hour</span>
          </ListGroup.Item>
        ))}
      </ListGroup>

      <Card.Footer className="text-center">
        <Button
          variant="primary"
          className="w-100"
          onClick={() => setIsModalOpen(true)}
        >
          Book Appointment
        </Button>
      </Card.Footer>

      <BookingModal
        doctor={doctor}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      />
    </Card>
  );
};

export default DoctorCard;