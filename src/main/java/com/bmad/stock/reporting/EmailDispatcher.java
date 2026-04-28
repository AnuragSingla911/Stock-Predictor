package com.bmad.stock.reporting;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailDispatcher {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@bmad-stock.com}")
    private String fromEmail;

    public void sendReport(String to, String subject, String htmlContent) {
        log.info("Sending email report to: {}", to);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, 
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                StandardCharsets.UTF_8.name());

            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail"));
            helper.setTo(Objects.requireNonNull(to, "to"));
            helper.setSubject(Objects.requireNonNull(subject, "subject"));
            helper.setText(Objects.requireNonNull(htmlContent, "htmlContent"), true);

            mailSender.send(message);
            log.info("Email sent successfully.");
            
        } catch (Exception e) {
            log.error("Failed to send email report", e);
        }
    }
}
