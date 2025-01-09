import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import API_CONFIG from '../config/api.config';
import { Spinner, Card, Button, Container, Row, Col, Alert, Badge } from 'react-bootstrap';
import { useLocation } from 'react-router-dom';

const AppointmentDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [appointmentData, setAppointmentData] = useState(null);
  const [patientData, setPatientData] = useState(null);
  const [doctorData, setDoctorData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [canellingId, setCanellingId] = useState(null);
  const location = useLocation();
  const successMessage = location.state?.message;
  const messageType = location.state?.type; 


  useEffect(() => {
    const fetchAllData = async () => {
      try {
        const token = localStorage.getItem('token');

        if (!token) {
          throw new Error('Token not found');
        }

        const appointmentResponse = await fetch(
          `${API_CONFIG.BASE_URL}/api/v1/appointments/${id}`,
          {
            headers: {
              accept: '*/*',
              Authorization: `Bearer ${token}`,
              'ngrok-skip-browser-warning': true,
            },
          }
        );

        if (!appointmentResponse.ok) {
          throw new Error('Failed to fetch appointment');
        }

        const appointmentData = await appointmentResponse.json();
        setAppointmentData(appointmentData);

        const userResponse = await fetch(
          `${API_CONFIG.BASE_URL}/api/v1/users/me`,
          {
            headers: {
              accept: '*/*',
              Authorization: `Bearer ${token}`,
              'ngrok-skip-browser-warning': true,
            },
          }
        );

        if (!userResponse.ok) {
          throw new Error('Failed to fetch patient');
        }

        const userData = await userResponse.json();
        setPatientData(userData);

        if (appointmentData.doctor_id) {
          const doctorResponse = await fetch(
            `${API_CONFIG.BASE_URL}/api/v1/doctor/${appointmentData.doctor_id}`,
            {
              headers: {
                accept: '*/*',
                Authorization: `Bearer ${token}`,
                'ngrok-skip-browser-warning': true,
              },
            }
          );

          if (!doctorResponse.ok) {
            throw new Error('Failed to fetch doctor');
          }

          const doctorData = await doctorResponse.json();
          setDoctorData(doctorData);
        }
      } catch (error) {
        setError(error.message || 'Failed to fetch appointment details');
      } finally {
        setLoading(false);
      }
    };

    fetchAllData();
  }, [id]);

  const handlePayment = async () => {
    const paymentUrl = appointmentData?.payment_details?.payment_url;
    if (paymentUrl) {
      window.open(paymentUrl, '_blank');
    }
  };

  const shouldShowPaymentButton = () => {
    const isAppointmentPending = appointmentData?.status === 'PENDING';
    const isPaymentPending = appointmentData?.payment_details?.status === 'PENDING';
    const hasPaymentUrl = Boolean(appointmentData?.payment_details?.payment_url);
    return isAppointmentPending && isPaymentPending && hasPaymentUrl;
  };

  const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    const date = new Date(dateString);
    return date.toLocaleDateString('id-ID', options);
  };

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center min-vh-100">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="d-flex justify-content-center align-items-center min-vh-100">
        <Alert variant="danger" className="text-center">
          <p>{error}</p>
          <Button variant="link" onClick={() => navigate('/home')}>
            Return to Home
          </Button>
        </Alert>
      </Container>
    );
  }

const handleCancel = async (appointmentId) => {
  if(!window.confirm('Are you sure you want to cancel this appointment?')) {
    return;
  }

  setCanellingId(appointmentId);
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_CONFIG.BASE_URL}/api/v1/appointments/cancel/${appointmentId}`, {
      headers: {
        httpMethod: 'PUT',
        accept: '*/*',
        Authorization: `Bearer ${token}`,
        'ngrok-skip-browser-warning': true,
      },
    });

    if (!response.ok) {
      throw new Error("Failed to cancel appointment");
    }

    navigate( `/appointments/${appointmentId}`, {
      state: {
        message: "Appointment has been cancelled successfully",
        type: "success",
      },
    });

  } catch (error) {
    setError(error.message);
  }finally {
    setCanellingId(null);
  }
};



  if (!appointmentData || !patientData) {
    return (
      <Container className="d-flex justify-content-center align-items-center min-vh-100">
        <Alert variant="danger" className="text-center">
          <p>Appointment or patient data not found</p>
          <Button variant="link" onClick={() => navigate('/home')}>
            Return to Home
          </Button>
        </Alert>
      </Container>
    );
  }

  return (
    <>
      {successMessage && (
        <Container className="my-3">
          <Alert variant={messageType === 'success' ? 'success' : 'info'} className="text-center">
            {successMessage}
          </Alert>
        </Container>
      )}
      <Container className="py-4 d-flex justify-content-center">
        <Card style={{ maxWidth: '600px', width: '100%' }}>
          <Card.Header className="bg-primary text-white">Appointment Details</Card.Header>
          <Card.Body>
            <Row className="mb-3">
              <Col>
                <h6>Patient</h6>
                <p>{patientData.user_name}</p>
              </Col>
              <Col>
                <h6>Doctor</h6>
                <p>{doctorData?.name || 'Loading...'}</p>
              </Col>
            </Row>

            <Row className="mb-3">
              <Col>
                <h6>Hospital</h6>
                <p>{doctorData?.hospital_name || 'Loading...'}</p>
              </Col>
              <Col>
                <h6>Consultation Type</h6>
                <p>{appointmentData.consultation_type}</p>
              </Col>
            </Row>

            <Row className="mb-3">
              <Col>
                <h6>Date</h6>
                <p>{formatDate(appointmentData.appointment_date)}</p>
              </Col>
              <Col>
                <h6>Time</h6>
                <p>{`${appointmentData.start_time + "0"} - ${appointmentData.end_time + "0"}`}</p>
              </Col>
            </Row>

            <Row className="mb-3">
              <Col>
                <h6>Appointment Status</h6>
                <Badge bg={
                  appointmentData.status === 'PENDING'
                    ? 'warning'
                    : appointmentData.status === 'CONFIRMED'
                    ? 'success'
                    : 'secondary'
                }>
                  {appointmentData.status}
                </Badge>
              </Col>
              <Col>
                <h6>Payment Status</h6>
                <Badge bg={
                  appointmentData.payment_details.status === 'PENDING'
                    ? 'warning'
                    : appointmentData.payment_details.status === 'PAID'
                    ? 'success'
                    : 'secondary'
                }>
                  {appointmentData.payment_details.status}
                </Badge>
              </Col>
            </Row>

            {appointmentData.payment_details && (
              <>
                <Row className="mb-3">
                  <Col>
                    <h6>Payment Amount</h6>
                    <p>
                      {new Intl.NumberFormat('id-ID', {
                        style: 'currency',
                        currency: 'IDR',
                      }).format(appointmentData.payment_details.amount)}
                    </p>
                  </Col>
                </Row>
              </>
            )}

            {shouldShowPaymentButton() && (
              <Button
                variant="primary"
                className="w-100"
                onClick={handlePayment}
              >
                Proceed to Payment
              </Button>
            )}

            <div className="text-center mt-3">
              <Button variant="link" onClick={() => navigate('/appointments')}>
                Back to Appointment List
              </Button>
            </div>
          </Card.Body>
        </Card>
      </Container>
    </>
  );

};

export default AppointmentDetail;
