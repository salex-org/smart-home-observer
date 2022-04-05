package org.salex.hmip.observer.test;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.OperatingMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.salex.hmip.observer.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

public class TestMailPublishService {
    private ContentGenerator contentGenerator;

    private JavaMailSender mailSender;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setup() throws IOException {
        this.contentGenerator = mock(ContentGenerator.class);
        this.mailSender = mock(JavaMailSender.class);
        this.mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void should_generate_email_when_climate_alert_has_to_be_sent() {
        // Prepare the test data
        final var now = new Date();
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));

        // Prepare the mocks
        when(contentGenerator.generateClimateAlert(any(Date.class), any(Date.class), any())).thenReturn(Mono.just("Some mail content"));

        // Create and call the service
        final var service = new DefaultMailPublishService(contentGenerator, mailSender, List.of("test@mail.address"));
        StepVerifier
                .create(service.sendClimateAlert(twentyMinutesAgo, now, Map.of()))
                .expectNextCount(1)
                .verifyComplete();

        // Verfication
        verify(contentGenerator, times(1)).generateClimateAlert(any(Date.class), any(Date.class), any());
        verifyNoMoreInteractions(contentGenerator);
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verifyNoMoreInteractions(mailSender);
    }

    @Test
    void should_do_nothing_when_climate_data_is_empty() {
        // Prepare the test data
        final var now = new Date();
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));

        // Prepare the mocks
        when(contentGenerator.generateClimateAlert(any(Date.class), any(Date.class), any())).thenReturn(Mono.empty());

        // Create and call the service
        final var service = new DefaultMailPublishService(contentGenerator, mailSender, List.of("test@mail.address"));
        StepVerifier
                .create(service.sendClimateAlert(twentyMinutesAgo, now, Map.of()))
                .expectNextCount(1)
                .verifyComplete();

        // Verfication
        verify(contentGenerator, times(1)).generateClimateAlert(any(Date.class), any(Date.class), any());
        verifyNoMoreInteractions(contentGenerator);
        verifyNoInteractions(mailSender);

    }

    @Test
    void should_generate_email_when_operating_alert_has_to_be_sent() {
        // Prepare the test data
        final List<OperatingAlertService.Event> data = List.of(new OperatingAlertService.Error(new RuntimeException("Some test exception")));

        // Prepare the mocks
        when(contentGenerator.generateOperatingAlert(any())).thenReturn(Mono.just("Some mail content"));

        // Create and call the service
        final var service = new DefaultMailPublishService(contentGenerator, mailSender, List.of("test@mail.address"));
        StepVerifier
                .create(service.sendOperatingAlert(data))
                .expectNextCount(1)
                .verifyComplete();

        // Verfication
        verify(contentGenerator, times(1)).generateOperatingAlert(any());
        verifyNoMoreInteractions(contentGenerator);
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verifyNoMoreInteractions(mailSender);
    }
}
