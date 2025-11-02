package com.learning.service.identity.serviceImp;


import com.learning.service.identity.EmailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {
    @Override
    public void sendOtp(String email, String otp) {
        String subject = "Your Login Verification Code";
        String body = String.format(
                "Your verification code is: %s\n\n" +
                        "This code will expire in 5 minutes.\n\n" +
                        "If you didn't request this code, please ignore this email.",
                otp
        );

        // For now, just log the email
        System.out.println("Sending OTP email to: " + email);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);

        // TODO: Implement actual email sending
        // emailSender.send(email, subject, body);
    }

    @Override
    public void sendBackupCode(String email, List<String> backupCodes) {
        String subject = "Your Two-Factor Authentication Backup Codes";
        StringBuilder body = new StringBuilder();
        body.append("Here are your backup codes for two-factor authentication:\n\n");

        for (int i = 0; i < backupCodes.size(); i++) {
            body.append(String.format("%d. %s\n", i + 1, backupCodes.get(i)));
        }

        body.append("\nSave these codes in a secure place. Each code can be used once.\n");
        body.append("If you lose your backup codes, you can generate new ones from your account settings.");

        // For now, just log the email
        System.out.println("Sending backup codes email to: " + email);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body.toString());

        // TODO: Implement actual email sending
        // emailSender.send(email, subject, body.toString());
    }
}
