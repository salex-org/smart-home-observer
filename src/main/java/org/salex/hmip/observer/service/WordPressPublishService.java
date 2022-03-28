package org.salex.hmip.observer.service;

import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.blog.Post;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class WordPressPublishService implements BlogPublishService {
    private final static String OVERVIEW_ID = "146";
    private final static String OVERVIEW_TYPE = "content_block";
    private final static String DETAILS_ID = "148";
    private final static String DETAILS_TYPE = "pages";
    private final static String HISTORY_ID = "60309";
    private final static String HISTORY_TYPE = "pages";
    private final static String REFERENCED_IMAGES_SEPARATOR = ";";

    private final WebClient client;

    public WordPressPublishService(WebClient client) {
        this.client = client;
    }

    @Override
    public Mono<Reading> postOverview(Reading reading) {
        return Mono.just(generateOverview(reading))
                .flatMap(content -> updatePost(OVERVIEW_ID, OVERVIEW_TYPE, content, new ArrayList<>()))
                .then(Mono.just(reading));
    }

    @Override
    public Mono<Void> postDetails(List<ClimateMeasurement> measurements) {
        // TODO impelement
        return Mono.empty();
    }

    private Mono<Post> getPost(String id, String type) {
        return this.client.get().uri("/" + type + "/" + id).retrieve().bodyToMono(Post.class);
    }

    private Mono<Void> updatePost(String id, String type, String content, List<Image> images) {
        return getPost(id, type)
                .flatMap(post -> {
                    final var oldReferencedImages = getReferencedImages(post);
                    post.setContent(content);
                    setReferencedImages(post, images);
                    return this.client
                            .post()
                            .uri("/" + type + "/" + id)
                            .bodyValue(post)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .then(Mono.just(oldReferencedImages));
                })
                .flatMapMany(Flux::just)
                .flatMap(this::deleteImage)
                .then(Mono.empty());
    }

    private Mono<Void> deleteImage(String id) {
        return this.client.delete().uri("/media/" + id + "?force=true").retrieve().bodyToMono(Void.class);
    }

    private String[] getReferencedImages(Post post) {
        if(post != null) {
            final var meta = post.getMeta();
            if(meta != null) {
                final var referencedImages = meta.getReferencedImages();
                if(referencedImages != null) {
                    return referencedImages.split(REFERENCED_IMAGES_SEPARATOR);
                }
            }
        }
        return new String[0];
    }

    private void setReferencedImages(Post post, List<Image> images) {
        if(images != null && post != null) {
            if(!images.isEmpty()) {
                final var meta = post.getMeta();
                if(meta != null) {
                    meta.setReferencedImages(getRefrencedImages(images));
                }
            }
        }
    }

    private String getRefrencedImages(List<Image> images) {
        final StringBuffer answer = new StringBuffer();
        boolean isFirst = true;
        for (Image each : images) {
            if (isFirst) {
                isFirst = false;
            } else {
                answer.append(REFERENCED_IMAGES_SEPARATOR);
            }
            answer.append(each.getId());
        }
        return answer.toString();
    }

    private String generateOverview(Reading reading) {
        final StringBuilder content = new StringBuilder();
        content.append("<span class=\"salex_no-series-meta-information\">");
        for(var measurement : reading.getMeasurements()) {
            if(measurement instanceof ClimateMeasurement) {
                final var climateMeasurement = (ClimateMeasurement) measurement;
                content.append("<p style=\"text-align: left;\">");
                content.append(climateMeasurement.getSensor().getName());
                content.append(": <strong><span style=\"color: ");
                content.append(PublishUtils.getTempColor(climateMeasurement.getTemperature()));
                content.append(";\">");
                content.append(PublishUtils.temperatureFormatter.format(climateMeasurement.getTemperature()));
                content.append(" °C</span></strong> bei <span style=\"color: ");
                content.append(PublishUtils.getHumidityColor(climateMeasurement.getHumidity()));
                content.append(";\">");
                content.append(PublishUtils.humidityFormatter.format(climateMeasurement.getHumidity()));
                content.append(" %</span></p>");
            }
        }
        content.append("<p style=\"text-align: left;\"><span style=\"color: #808080;\">Gemessen am ");
        content.append(PublishUtils.dateFormatter.format(reading.getReadingTime()));
        content.append(" um ");
        content.append(PublishUtils.timeFormatter.format(reading.getReadingTime()));
        content.append("</span></p>");
        content.append("<p style=\"text-align: left;\"><a href=\"https://holzwerken.salex.org/werkstattklima\">Details ansehen</a></p>");
        content.append("<p style=\"text-align: left;\"><a href=\"https://holzwerken.salex.org/werkstattklima-entwicklung\">Jahresverlauf ansehen</a></p>");
        content.append("</span>");
        return content.toString();
    }

    // Old Content Generator:
//

//
//    public String generateDetails(List<Measurement> data, Image diagram) {
//        final StringBuffer content = new StringBuffer();
//        Collections.sort(data, new Comparator<Measurement>() {
//            public int compare(Measurement one, Measurement another) {
//                return one.getTimestamp().compareTo(another.getTimestamp());
//            }
//        });
//        if(!data.isEmpty()) {
//            final Date periodStart = data.get(0).getTimestamp();
//            final Date periodEnd = data.get(data.size()-1).getTimestamp();
//            content.append("<h3>Zeitraum von ");
//            content.append(PublishUtils.timestampFormatter.format(periodStart));
//            content.append(" bis ");
//            content.append(PublishUtils.timestampFormatter.format(periodEnd));
//            content.append("</h3>");
//            content.append("<img class=\"aligncenter size-full wp-image-");
//            content.append(diagram.getId());
//            content.append("\" src=\"");
//            content.append(diagram.getFull());
//            content.append("\" alt=\"\" width=\"600\" height=\"300\" />");
//            PublishUtils.appendTable(content, data, sensors);
//        } else {
//            content.append("<h3>Keine Daten vorhanden</h3>");
//        }
//        return content.toString();
//    }
//
//    public String generateHistory(Map<Sensor, List<BoundaryReading>> data, Map<Sensor, Map<String, Image>> diagrams) {
//        final StringBuffer content = new StringBuffer();
//        final List<BoundaryReading> all = new ArrayList<BoundaryReading>();
//        for(List<BoundaryReading> each : data.values()) {
//            all.addAll(each);
//        }
//        Collections.sort(all, new Comparator<BoundaryReading>() {
//            public int compare(BoundaryReading one, BoundaryReading another) {
//                return one.getDay().compareTo(another.getDay());
//            }
//        });
//        if(!data.isEmpty()) {
//            final Date periodStart = all.get(0).getDay();
//            final Date periodEnd = all.get(all.size()-1).getDay();
//            content.append("<h3>Zeitraum von ");
//            content.append(PublishUtils.dateFormatter.format(periodStart));
//            content.append(" bis ");
//            content.append(PublishUtils.dateFormatter.format(periodEnd));
//            content.append("</h3>");
//            PublishUtils.appendTable(content, data, diagrams, this.sensors);
//        } else {
//            content.append("<h3>Keine Daten vorhanden</h3>");
//        }
//        return content.toString();
//    }
}
