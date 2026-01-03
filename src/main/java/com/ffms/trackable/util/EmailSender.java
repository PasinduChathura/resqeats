package com.ffms.trackable.util;

import com.ffms.trackable.common.dto.MailDto;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
public class EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendEmail(MailDto mailDto) throws Exception {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(mailDto.getTo());
            helper.setCc(mailDto.getCc());
            helper.setBcc(mailDto.getBcc());
            helper.setText(mailDto.getBody(), false);
            helper.setSubject(mailDto.getSubject());
            helper.setFrom(mailDto.getFrom());

            javaMailSender.send(message);
        } catch (MailSendException mailSendException) {
            LOGGER.error("Error occurred while sending email: {}", mailSendException.getMessage(), mailSendException);
            throw new MailSendException("Error occurred while sending email! ",mailSendException);
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.error("Error occurred while sending email: {}", illegalArgumentException.getMessage(), illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage(), illegalArgumentException);
        } catch (Exception exception) {
            LOGGER.error("Error occurred while sending email", exception);
            throw exception;
        }
    }

    public static String[] commaSeparatedStringToArray(String commaSeparatedString) {
        return commaSeparatedString == null || commaSeparatedString.isEmpty() ? new String[]{} : commaSeparatedString.trim().split("\\s*,\\s*");
    }

}
