package com.oneDev.healthcarebooking.controller;

import com.oneDev.healthcarebooking.model.response.PaymentNotification;
import com.oneDev.healthcarebooking.service.PaymentService;
import com.oneDev.healthcarebooking.service.XenditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/xendit")
@RequiredArgsConstructor
public class WebhookController {

    private final XenditService xenditService;

    @PostMapping
    public ResponseEntity<String> handleXenditWebhook(
            @RequestBody PaymentNotification paymentNotification
    ) {
        try{
            xenditService.handlePaymentNotification(paymentNotification);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }

    }
}
