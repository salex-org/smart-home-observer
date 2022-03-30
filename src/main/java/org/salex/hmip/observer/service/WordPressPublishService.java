package org.salex.hmip.observer.service;

import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.blog.Media;
import org.salex.hmip.observer.blog.Post;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class WordPressPublishService implements BlogPublishService {
    private final static String OVERVIEW_ID = "146";
    private final static String OVERVIEW_TYPE = "content_block";
    private final static String DETAILS_ID = "148";
    private final static String DETAILS_TYPE = "pages";
    private final static String HISTORY_ID = "60309";
    private final static String HISTORY_TYPE = "pages";
    private final static String REFERENCED_IMAGES_SEPARATOR = ";";

    private final WebClient client;

    private final ContentGenerator contentGenerator;

    private final ChartGenerator chartGenerator;

    public WordPressPublishService(WebClient client, ContentGenerator contentGenerator, ChartGenerator chartGenerator) {
        this.client = client;
        this.contentGenerator = contentGenerator;
        this.chartGenerator = chartGenerator;
    }

    @Override
    public Mono<Reading> postOverview(Reading reading) {
        return contentGenerator.generateOverview(reading)
                .flatMap(content -> updatePost(OVERVIEW_ID, OVERVIEW_TYPE, content, new ArrayList<>()))
                .then(Mono.just(reading));
    }

    @Override
    public Mono<Map<Sensor, List<ClimateMeasurement>>> postDetails(Date start, Date end, Map<Sensor, List<ClimateMeasurement>> data) {
        return Mono.just(this.chartGenerator)
                        .flatMap(generator -> {
                            try {
                                return addPNGImage("verlauf-", generator.create24HourChart(start, end, data));
                            } catch (IOException e) {
                                return Mono.error(e);
                            }
                        })
                        .flatMap(image -> contentGenerator.generateDetails(start, end, data, image)
                                .flatMap(content -> updatePost(DETAILS_ID, DETAILS_TYPE, content, List.of(image)))
                                .then(Mono.just(data))
                        );
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

    private Mono<Image> addPNGImage(String prefix, byte[] data) {
        final var filename = prefix + UUID.randomUUID() + ".png";
        return this.client.post()
                .uri("/media")
                .contentType(MediaType.IMAGE_PNG)
                .header("content-disposition", "attachement; filename=" + filename)
                .bodyValue(data)
                .exchangeToMono(clientResponse -> {
                    if(clientResponse.statusCode() == HttpStatus.CREATED) {
                        return Mono.just(clientResponse.headers().header("Location"));
                    } else {
                        return Mono.error(new Exception("Error adding png image to blog"));
                    }
                })
                .mapNotNull(location -> location.stream().findFirst().orElse(null))
                .mapNotNull(location -> Stream.of(location.split("/")).reduce( (first, last) -> last ))
                .mapNotNull(Optional::get)
                .flatMap(imageId -> this.client.get()
                            .uri("/media/" + imageId)
                            .retrieve()
                            .bodyToMono(Media.class)
                            .map(media -> {
                                final var image = new Image(imageId);
                                if (media.getDetails().getSizes().containsKey("full")) {
                                    image.setFull(media.getDetails().getSizes().get("full").getUrl());
                                }
                                if (media.getDetails().getSizes().containsKey("thumbnail")) {
                                    image.setThumbnail(media.getDetails().getSizes().get("thumbnail").getUrl());
                                    image.setThumbnailWidth(media.getDetails().getSizes().get("thumbnail").getWidth());
                                    image.setThumbnailHeight(media.getDetails().getSizes().get("thumbnail").getHeight());
                                }
                                return image;
                            })
                );
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
        final var answer = new StringBuilder();
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
}
