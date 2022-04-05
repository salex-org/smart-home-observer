package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Sensor;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
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

    private final InternetAddress[] alarmMailTargets;

    public DefaultMailPublishService(ContentGenerator contentGenerator, JavaMailSender mailSender, List<String> alarmMailTargets) {
        this.mailSender = mailSender;
        this.contentGenerator = contentGenerator;
        this.alarmMailTargets = alarmMailTargets.stream().map(this::createAddress).toList().toArray(new InternetAddress[0]);
    }

    @Override
    public Mono<Map<Sensor, List<ClimateMeasurement>>> sendClimateAlert(Date start, Date end, Map<Sensor, List<ClimateMeasurement>> data) {
        return contentGenerator.generateClimateAlert(start, end, data)
                .mapNotNull(content -> content)
                .flatMap(content -> sendMail("Klimaalarm", content))
                .then(Mono.just(data));
    }

    @Override
    public Mono<List<OperatingAlertService.Event>> sendOperatingAlert(List<OperatingAlertService.Event> data) {
        return contentGenerator.generateOperatingAlert(data)
                .mapNotNull(content -> content)
                .flatMap(content -> sendMail("Betriebsalarm", content))
                .then(Mono.just(data));
    }

    private InternetAddress createAddress(String address) {
        try {
            return new InternetAddress(address);
        } catch(AddressException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<String> sendMail(String subject, String content) {
        try {
            final var textPart = new MimeBodyPart();
            final var message = this.mailSender.createMimeMessage();
            textPart.setContent(content, "text/html");
            message.setFrom(new InternetAddress("noreply@salex.org", "Smart Home Observer"));
            message.setHeader("X-Priority", "1");
            message.setRecipients(Message.RecipientType.TO, this.alarmMailTargets);
            message.setSubject(subject);
            message.setContent(new MimeMultipart(textPart));
            this.mailSender.send(message);
            return Mono.just(content);
        } catch (MessagingException | UnsupportedEncodingException e) {
            return Mono.error(e);
        }
    }
}
