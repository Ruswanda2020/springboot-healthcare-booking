package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.Appointment;
import com.oneDev.healthcarebooking.entity.Payment;
import com.oneDev.healthcarebooking.entity.User;
import com.oneDev.healthcarebooking.enumaration.AppointmentStatus;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.PaymentStatus;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.response.PaymentNotification;
import com.oneDev.healthcarebooking.model.response.PaymentResponse;
import com.oneDev.healthcarebooking.repository.AppointmentRepository;
import com.oneDev.healthcarebooking.repository.PaymentRepository;
import com.oneDev.healthcarebooking.repository.UserRepository;
import com.oneDev.healthcarebooking.service.XenditService;
import com.xendit.exception.XenditException;
import com.xendit.model.Invoice;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class XenditPaymentServiceImpl implements XenditService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override @Transactional
    public PaymentResponse createPayment(Payment payment) {

        // 1. Mengambil data Appointment berdasarkan ID yang ada di objek Payment
        Appointment appointment = appointmentRepository.findById(payment.getAppointmentId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Appointment not found with id: " + payment.getAppointmentId()));

        // 2. Mengambil data User berdasarkan patientId dari Appointment
        User user = userRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "User not found with id: " + appointment.getPatientId()));

        // 3. Menyiapkan parameter untuk invoice Xendit
        Map<String, Object> params = new HashMap<>();
        params.put("external_id", payment.getTransactionId());
        params.put("amount", payment.getAmount().doubleValue());
        params.put("payer_email", user.getEmail());
        params.put("description", "Payment for order #" + payment.getTransactionId());

        // 4. Membuat invoice menggunakan Xendit API
        Invoice invoice = null;
        try {
            // Mengirim request untuk membuat invoice Xendit
            invoice = Invoice.create(params);
        } catch (XenditException e) {
            // 5. Jika ada error saat membuat invoice, lemparkan exception
            throw new ApplicationException(ExceptionType.PAYMENT_NOT_FOUND, e.getMessage());
        }

        // 6. Menyimpan informasi invoice Xendit ke dalam objek Payment
        payment.setXenditInvoiceId(invoice.getId());
        payment.setXenditPaymentStatus(invoice.getStatus());

        // 7. Menyimpan objek Payment ke dalam database
        paymentRepository.save(payment);

        // 8. Menyiapkan dan mengembalikan response PaymentResponse
        PaymentResponse response = PaymentResponse.from(payment);
        //9. Menambahkan URL pembayaran dari invoice Xendit ke response
        response.setPaymentUrl(invoice.getInvoiceUrl());
        return response;
    }


    @Override @Transactional
    public void handlePaymentNotification(PaymentNotification payload) {

        String invoiceId = payload.getId();
        String status = payload.getStatus();

        //fetch the order
        Payment payment = paymentRepository.findByXenditInvoiceId(invoiceId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Order not found with id: " + invoiceId));

        payment.setXenditPaymentStatus(status);
        switch (status){
            case "PAID":
                handleOnSuccess(payment);
                break;
            case "EXPIRED":
                payment.setStatus(PaymentStatus.CANCELLED);
                break;
            case "FAILED":
                payment.setStatus(PaymentStatus.FAILED);
                break;
            case "PENDING":
                payment.setStatus(PaymentStatus.PENDING);
                break;
            default:
        }

        //update payment method
        if (payload.getPaymentMethod() != null) {
            payment.setPaymentMethod(payload.getPaymentMethod());
        }

        //update & save
        paymentRepository.save(payment);

    }

    private void handleOnCancellation(Payment payment) {
        payment.setStatus(PaymentStatus.CANCELLED);
        Appointment appointment = appointmentRepository.findById(payment.getAppointmentId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Appointment not found for transaction id: " + payment.getAppointmentId()));

        appointment.setStatus(AppointmentStatus.CANCELLED);

        try{
            Invoice.expire(payment.getXenditInvoiceId());
        } catch (XenditException e) {
            throw new ApplicationException(ExceptionType.PAYMENT_NOT_FOUND, e.getMessage());
        }
        appointmentRepository.save(appointment);
    }

    private void handleOnSuccess(Payment payment) {
        payment.setStatus(PaymentStatus.COMPLETED);

        Appointment appointment = appointmentRepository.findById(payment.getAppointmentId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Appointment not found for transaction id: " + payment.getAppointmentId()));

        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);
    }
}
