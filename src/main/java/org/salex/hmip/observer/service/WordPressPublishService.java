package org.salex.hmip.observer.service;

import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.blog.Post;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public Mono<Map<Sensor, List<ClimateMeasurement>>> postDetails(Map<Sensor, List<ClimateMeasurement>> data) {
        return contentGenerator.generateDetails(data, new Image("TODO")) // TODO impelement chart generation
                .flatMap(content -> Mono.empty()) // TODO implement blog-update
                .then(Mono.just(data));
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
}
