package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Sensor;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DefaultMailPublishService implements MailPublishService {
    private final JavaMailSender mailSender;

    private final ContentGenerator contentGenerator;

    private final String alarmMailTarget;

    public DefaultMailPublishService(ContentGenerator contentGenerator, JavaMailSender mailSender, String alarmMailTarget) {
        this.mailSender = mailSender;
        this.contentGenerator = contentGenerator;
        this.alarmMailTarget = alarmMailTarget;
    }

    @Override
    public Mono<Map<Sensor, List<ClimateMeasurement>>> sendClimateAlarm(Date start, Date end, Map<Sensor, List<ClimateMeasurement>> data) {
        return contentGenerator.generateAlarm(start, end, data)
                .mapNotNull(content -> content)
                .flatMap(content -> sendMail(content))
                .then(Mono.just(data));
    }

    private Mono<String> sendMail(String content) {
        try {
            final var textPart = new MimeBodyPart();
            final var message = this.mailSender.createMimeMessage();
            textPart.setContent(content, "text/html");
            message.setFrom(new InternetAddress("noreply@salex.org", "Smart Home Observer"));
            message.setHeader("X-Priority", "1");
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(this.alarmMailTarget));
            message.setSubject("Temperaturalarm");
            message.setContent(new MimeMultipart(textPart));
            this.mailSender.send(message);
            return Mono.just(content);
        } catch (MessagingException | UnsupportedEncodingException e) {
            return Mono.error(e);
        }
    }
}
