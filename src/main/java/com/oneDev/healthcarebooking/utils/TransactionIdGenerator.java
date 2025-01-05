package com.oneDev.healthcarebooking.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class TransactionIdGenerator {
    public static String generateTransactionId() {
        // Format tanggal untuk tahun dan bulan (yyMM)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMM");
        String datePart = dateFormat.format(new Date());

        // Format waktu untuk jam dan menit (HHmm)
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm");
        String timePart = timeFormat.format(new Date());

        // UUID untuk memastikan keunikan (ambil 6 karakter pertama)
        String uuidPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Gabungkan semuanya
        return "TXN-" + datePart + timePart + "-" + uuidPart;
    }
}
