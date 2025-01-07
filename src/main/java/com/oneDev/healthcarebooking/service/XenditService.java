package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.entity.Payment;
import com.oneDev.healthcarebooking.model.response.PaymentNotification;
import com.oneDev.healthcarebooking.model.response.PaymentResponse;

public interface XenditService {

    PaymentResponse createPayment(Payment payment);
    void handlePaymentNotification(PaymentNotification payload);
}
