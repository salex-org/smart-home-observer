package org.salex.hmip.observer.test;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.data.*;
import org.salex.hmip.observer.service.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TestBlogPublishService {
    private MockWebServer mockWebServer;
    private WebClient webClient;
    private ContentGenerator contentGenerator;
    private ChartGenerator chartGenerator;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        this.webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        this.contentGenerator = mock(ContentGenerator.class);
        this.chartGenerator = mock(ChartGenerator.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void should_generate_overview_for_reading() throws Exception {
        // Prepare the test data
        final var now = new Date();
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        reading.addMeasurement(new ClimateMeasurement(reading, firstSensor, now, 12.3, 42.7, 3.45674395764));
        reading.addMeasurement(new ClimateMeasurement(reading, secondSensor, now, 15.7, 43.5, 3.14159265358));
        reading.addMeasurement(new OperatingMeasurement(reading, 1.0, 2.0, 3.0, 4.0));

        // Prepare the mocks
        when(contentGenerator.generateOverview(reading)).thenReturn(Mono.just("some test content"));
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "overview-content-block.json", new String[][] { { "Content-Type", "application/json; charset=UTF-8" } } )); // Read old content
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, null)); // Post new content

        // Create and call the service
        final var service = new WordPressPublishService(webClient, contentGenerator, chartGenerator);
        StepVerifier
                .create(service.postOverview(reading))
                .expectNextCount(1)
                .verifyComplete();

        // Verfication
        verify(contentGenerator, times(1)).generateOverview(reading);
        verifyNoMoreInteractions(contentGenerator);
        verifyNoInteractions(chartGenerator);
        assertThat(this.mockWebServer.getRequestCount()).isEqualTo(2);
        final var readPostRequest = this.mockWebServer.takeRequest();
        final var updatePostRequest = this.mockWebServer.takeRequest();
        assertThat(readPostRequest.getMethod()).isEqualTo("GET");
        assertThat(readPostRequest.getPath()).isEqualTo("/content_block/146");
        assertThat(updatePostRequest.getMethod()).isEqualTo("POST");
        assertThat(updatePostRequest.getPath()).isEqualTo("/content_block/146");
    }

    @Test
    void should_generate_details_for_period() throws Exception {
        // Prepare the test data
        final var now = new Date();
        final var tenMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Second", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, List.of(
                        new ClimateMeasurement(reading, firstSensor, twentyMinutesAgo, 11.2, 52.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, tenMinutesAgo, 13.2, 42.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, now, 12.2, 32.7, 5.2386758493768)
                ),
                secondSensor, List.of(
                        new ClimateMeasurement(reading, secondSensor, twentyMinutesAgo, 21.2, 82.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, tenMinutesAgo, 23.2, 72.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, now, 22.2, 62.7, 5.2386758493768)
                )
        );

        // Prepare the mocks
        when(chartGenerator.create24HourChart(any(), any(), any())).thenReturn(Mono.just(new byte[0]));
        when(contentGenerator.generateDetails(any(), any(), any(), any(Image.class))).thenReturn(Mono.just("some test content"));
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.CREATED, "add-image-result.json", new String[][] { { "Location", "some-test-id/12345" }, { "Content-Type", "application/json; charset=UTF-8" }})); // Add new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "get-image-result.json", new String[][] { { "Content-Type", "application/json; charset=UTF-8" } })); // Read data for new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "details-page.json", new String[][] { { "Content-Type", "application/json; charset=UTF-8" } })); // Read old content
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, null)); // Post new content
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, null)); // Delete image 634535

        // Create and call the service
        final var service = new WordPressPublishService(webClient, contentGenerator, chartGenerator);
        StepVerifier
                .create(service.postDetails(tenMinutesAgo, now, data))
                .expectNextCount(1)
                .verifyComplete();

        // Verification
        verify(contentGenerator, times(1)).generateDetails(any(), any(), any(), any(Image.class));
        verifyNoMoreInteractions(contentGenerator);
        verify(chartGenerator, times(1)).create24HourChart(any(), any(), any());
        verifyNoMoreInteractions(chartGenerator);
        assertThat(this.mockWebServer.getRequestCount()).isEqualTo(5);
        final var addNewImageRequest = this.mockWebServer.takeRequest();
        final var readNewImageRequest = this.mockWebServer.takeRequest();
        final var readPostRequest = this.mockWebServer.takeRequest();
        final var updatePostRequest = this.mockWebServer.takeRequest();
        final var deleteOldImageRequest = this.mockWebServer.takeRequest();
        assertThat(addNewImageRequest.getMethod()).isEqualTo("POST");
        assertThat(addNewImageRequest.getPath()).isEqualTo("/media");
        assertThat(addNewImageRequest.getHeader("content-disposition")).startsWith("attachement; filename=verlauf-");
        assertThat(readNewImageRequest.getMethod()).isEqualTo("GET");
        assertThat(readNewImageRequest.getPath()).isEqualTo("/media/12345");
        assertThat(readPostRequest.getMethod()).isEqualTo("GET");
        assertThat(readPostRequest.getPath()).isEqualTo("/pages/148");
        assertThat(updatePostRequest.getMethod()).isEqualTo("POST");
        assertThat(updatePostRequest.getPath()).isEqualTo("/pages/148");
        assertThat(deleteOldImageRequest.getMethod()).isEqualTo("DELETE");
        assertThat(deleteOldImageRequest.getPath()).isEqualTo("/media/634535?force=true");
    }

    @Test
    void should_generate_history_for_period() throws Exception {
        // Prepare the test data
        final var now = new Date();
        final var yesterday = new Date(now.getTime() - TimeUnit.DAYS.toMillis(1));
        final var moreThanAYearAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(370));
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Second", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, List.of(
                        createBoundaries(firstSensor, now, 10.0, 15.0, 42.0, 56.0, 3.123, 5.321),
                        createBoundaries(firstSensor, yesterday, 12.0, 17.0, 47.0, 58.0, 4.123, 6.321),
                        createBoundaries(firstSensor, moreThanAYearAgo, 8.0, 13.0, 38.0, 49.0, 2.123, 4.321)
                ),
                secondSensor, List.of(
                        createBoundaries(secondSensor, now, 10.5, 15.5, 42.5, 56.5, 3.123, 5.321),
                        createBoundaries(secondSensor, yesterday, 12.5, 17.5, 47.5, 58.5, 4.123, 6.321),
                        createBoundaries(secondSensor, moreThanAYearAgo, 8.5, 13.5, 38.5, 49.5, 2.123, 4.321)
                ));

        // Prepare the mocks
        when(chartGenerator.create365DayHumidityChart(any(Date.class), any(Date.class), any(List.class), any(Sensor.class))).thenReturn(Mono.just(new byte[0]));
        when(chartGenerator.create365DayTemperatureChart(any(Date.class), any(Date.class), any(List.class), any(Sensor.class))).thenReturn(Mono.just(new byte[0]));
        when(contentGenerator.generateHistory(any(Date.class), any(Date.class), any(Map.class), any(Map.class))).thenReturn(Mono.just("some test content"));
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.CREATED, "add-image-result.json", new String[][] { { "Location", "some-test-id/12345" }, { "Content-Type", "application/json; charset=UTF-8" }})); // Add new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.CREATED, "add-image-result.json", new String[][] { { "Location", "some-test-id/12345" }, { "Content-Type", "application/json; charset=UTF-8" }})); // Add new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "get-image-result.json", new String[][] { { "Content-Type", "application/json; charset=UTF-8" } })); // Read data for new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "get-image-result.json", new String[][] { { "Content-Type", "application/json; charset=UTF-8" } })); // Read data for new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.CREATED, "add-image-result.json", new String[][] { { "Location", "some-test-id/12345" }, { "Content-Type", "application/json; charset=UTF-8" }})); // Add new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.CREATED, "add-image-result.json", new String[][] { { "Location", "some-test-id/12345" }, { "Content-Type", "application/json; charset=UTF-8" }})); // Add new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "get-image-result.json", new String[][] { { "Content-Type", "application/json; charset=UTF-8" } })); // Read data for new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "get-image-result.json", new String[][] { { "Content-Type", "application/json; charset=UTF-8" } })); // Read data for new image
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "history-page.json", new String[][] { { "Content-Type", "application/json; charset=UTF-8" } })); // Read old content
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, null)); // Post new content
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, null)); // Delete image 633623
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, null)); // Delete image 633624
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, null)); // Delete image 633625
        this.mockWebServer.enqueue(createMockResponse(HttpStatus.OK, null)); // Delete image 633626

        // Create and call the service
        final var service = new WordPressPublishService(webClient, contentGenerator, chartGenerator);
        StepVerifier
                .create(service.postHistory(yesterday, now, data))
                .expectNextCount(1)
                .verifyComplete();

        // Verification
        verify(contentGenerator, times(1)).generateHistory(any(Date.class), any(Date.class), any(Map.class), any(Map.class));
        verifyNoMoreInteractions(contentGenerator);
        verify(chartGenerator, times(2)).create365DayTemperatureChart(any(Date.class), any(Date.class), any(List.class), any(Sensor.class));
        verify(chartGenerator, times(2)).create365DayHumidityChart(any(Date.class), any(Date.class), any(List.class), any(Sensor.class));
        verifyNoMoreInteractions(chartGenerator);
        assertThat(this.mockWebServer.getRequestCount()).isEqualTo(14);
        final var addNewImageRequest1 = this.mockWebServer.takeRequest();
        final var addNewImageRequest2 = this.mockWebServer.takeRequest();
        final var readNewImageRequest1 = this.mockWebServer.takeRequest();
        final var readNewImageRequest2 = this.mockWebServer.takeRequest();
        final var addNewImageRequest3 = this.mockWebServer.takeRequest();
        final var addNewImageRequest4 = this.mockWebServer.takeRequest();
        final var readNewImageRequest3 = this.mockWebServer.takeRequest();
        final var readNewImageRequest4 = this.mockWebServer.takeRequest();
        final var readPostRequest = this.mockWebServer.takeRequest();
        final var updatePostRequest = this.mockWebServer.takeRequest();
        final var deleteOldImageRequest1 = this.mockWebServer.takeRequest();
        final var deleteOldImageRequest2 = this.mockWebServer.takeRequest();
        final var deleteOldImageRequest3 = this.mockWebServer.takeRequest();
        final var deleteOldImageRequest4 = this.mockWebServer.takeRequest();
        assertThat(addNewImageRequest1.getMethod()).isEqualTo("POST");
        assertThat(addNewImageRequest1.getPath()).isEqualTo("/media");
        assertThat(addNewImageRequest1.getHeader("content-disposition")).startsWith("attachement; filename=temperature-");
        assertThat(readNewImageRequest1.getMethod()).isEqualTo("GET");
        assertThat(readNewImageRequest1.getPath()).isEqualTo("/media/12345");
        assertThat(addNewImageRequest2.getMethod()).isEqualTo("POST");
        assertThat(addNewImageRequest2.getPath()).isEqualTo("/media");
        assertThat(addNewImageRequest2.getHeader("content-disposition")).startsWith("attachement; filename=temperature-");
        assertThat(readNewImageRequest2.getMethod()).isEqualTo("GET");
        assertThat(readNewImageRequest2.getPath()).isEqualTo("/media/12345");
        assertThat(addNewImageRequest3.getMethod()).isEqualTo("POST");
        assertThat(addNewImageRequest3.getPath()).isEqualTo("/media");
        assertThat(addNewImageRequest3.getHeader("content-disposition")).startsWith("attachement; filename=humidity-");
        assertThat(readNewImageRequest3.getMethod()).isEqualTo("GET");
        assertThat(readNewImageRequest3.getPath()).isEqualTo("/media/12345");
        assertThat(addNewImageRequest4.getMethod()).isEqualTo("POST");
        assertThat(addNewImageRequest4.getPath()).isEqualTo("/media");
        assertThat(addNewImageRequest4.getHeader("content-disposition")).startsWith("attachement; filename=humidity-");
        assertThat(readNewImageRequest4.getMethod()).isEqualTo("GET");
        assertThat(readNewImageRequest4.getPath()).isEqualTo("/media/12345");
        assertThat(readPostRequest.getMethod()).isEqualTo("GET");
        assertThat(readPostRequest.getPath()).isEqualTo("/pages/60309");
        assertThat(updatePostRequest.getMethod()).isEqualTo("POST");
        assertThat(updatePostRequest.getPath()).isEqualTo("/pages/60309");
        assertThat(deleteOldImageRequest1.getMethod()).isEqualTo("DELETE");
        assertThat(deleteOldImageRequest1.getPath()).startsWith("/media/63362");
        assertThat(deleteOldImageRequest1.getPath()).endsWith("force=true");
        assertThat(deleteOldImageRequest2.getMethod()).isEqualTo("DELETE");
        assertThat(deleteOldImageRequest2.getPath()).startsWith("/media/63362");
        assertThat(deleteOldImageRequest2.getPath()).endsWith("force=true");
        assertThat(deleteOldImageRequest3.getMethod()).isEqualTo("DELETE");
        assertThat(deleteOldImageRequest3.getPath()).startsWith("/media/63362");
        assertThat(deleteOldImageRequest3.getPath()).endsWith("force=true");
        assertThat(deleteOldImageRequest4.getMethod()).isEqualTo("DELETE");
        assertThat(deleteOldImageRequest4.getPath()).startsWith("/media/63362");
        assertThat(deleteOldImageRequest4.getPath()).endsWith("force=true");
    }

    private static MockResponse createMockResponse(HttpStatus status, String resultJson, String[]... headers) {
        try {
            final var mockResponse = new MockResponse();
            mockResponse.setResponseCode(status.value());
            if (resultJson != null) {
                final var resultJsonFile = new ClassPathResource("TestBlogPublishService/" + resultJson).getFile();
                final var body = Files.readString(resultJsonFile.toPath());
                mockResponse.setBody(body);
            }
            for (var header : headers) {
                mockResponse.setHeader(header[0], header[1]);
            }
            return mockResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClimateMeasurementBoundaries createBoundaries(Sensor sensor, Date day, Double minTemp, Double maxTemp, Double minHum, Double maxHum, Double minVap, Double maxVap) {
        return new ClimateMeasurementBoundaries() {
            @Override
            public Double getMinimumTemperature() {
                return minTemp;
            }
            @Override
            public Double getMaximumTemperature() {
                return maxTemp;
            }
            @Override
            public Double getMinimumHumidity() {
                return minHum;
            }
            @Override
            public Double getMaximumHumidity() {
                return maxHum;
            }
            @Override
            public Double getMinimumVaporAmount() {
                return minVap;
            }
            @Override
            public Double getMaximumVaporAmount() {
                return maxVap;
            }
            @Override
            public Long getSensorId() {
                return sensor.getId();
            }
            @Override
            public Date getDay() {
                return day;
            }
        };
    }
}
