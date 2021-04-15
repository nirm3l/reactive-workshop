package com.example.demo.services;

import com.example.demo.model.WikiRecord;
import com.example.demo.properties.WikiProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WikiService {

    private final WebClient wikiClient;

    private final Sinks.Many<String> wikis;

    private final Map<String, Sinks.Many<String>> titlesMap = new ConcurrentHashMap<>();

    private final MeterRegistry meterRegistry;

    private final WikiProperties wikiProperties;

    private final WebClient webContentClient = WebClient.builder().build();

    private final Scheduler processUrlScheduler = Schedulers.newBoundedElastic(100, 100000, "process-url");

    private final Scheduler detectLanguageScheduler = Schedulers.newBoundedElastic(100, 100000, "detect-language");

    private final LanguageDetector languageDetector;

    public WikiService(WebClient wikiClient, MeterRegistry meterRegistry, WikiProperties wikiProperties, LanguageDetector languageDetector) {
        this.wikiClient = wikiClient;
        this.meterRegistry = meterRegistry;
        this.wikiProperties = wikiProperties;
        this.languageDetector = languageDetector;
        wikis = Sinks.many().multicast().directBestEffort();

        titlesMap.put("all", Sinks.many().multicast().directBestEffort());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        getEvents()
                .filter(event -> event.getWiki() != null && event.getTitle() != null)
                .doOnNext(event -> {
                    wikis.tryEmitNext(event.getWiki());
                    titlesMap.get("all").tryEmitNext(event.getTitle());
                })
                .flatMap(event -> wikiProperties.isLoadContent() ? loadContent(event) : Mono.just(event))
                .flatMap(this::detectLanguage)
                .doOnNext(event -> {
                    if(!titlesMap.containsKey(event.getWiki())) {
                        titlesMap.put(event.getWiki(), Sinks.many().multicast().directBestEffort());
                    }

                    titlesMap.get(event.getWiki()).tryEmitNext(event.getTitle());

                    addTags(event);
                })
                .subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    public void addTags(WikiRecord event) {
        Tags tags = Tags.of(Tag.of("name", event.getWiki()));

        if(Arrays.asList("bswiki", "hrwiki", "srwiki").contains(event.getWiki()) && event.getUser() != null) {
            tags = tags.and(Tag.of("user", event.getUser()));
        } else {
            tags = tags.and(Tag.of("user", "UNKNOWN"));
        }

        tags = tags.and(Tag.of("content", event.getContent() != null ? "YES" : "NO"));
        tags = tags.and(Tag.of("language", event.getLanguage() != null ? event.getLanguage() : "UNKNOWN"));

        meterRegistry.counter("wikis", tags).increment();
    }

    public Mono<WikiRecord> loadContent(WikiRecord event) {
        if(event.getMeta() == null || event.getMeta().getUri() == null) {
            return Mono.just(event);
        }

        return webContentClient.get().uri(event.getMeta().getUri())
                .retrieve().bodyToMono(String.class)
                .flatMap(content -> Mono.<WikiRecord>create(sink -> {
                    Document doc = Jsoup.parse(content);

                    Element contentElement = doc.getElementById("content");

                    event.setContent(contentElement != null ? contentElement.text() : doc.text());

                    sink.success(event);
                }))
                .onErrorResume(e -> Mono.just(event))
                .subscribeOn(processUrlScheduler);
    }

    public Mono<WikiRecord> detectLanguage(WikiRecord event) {
        return Mono.create(sink -> {
            if(event.getContent() == null) {
                sink.success();
            } else {
                Language language = languageDetector.predictLanguage(event.getContent());

                event.setLanguage(language.getLang());

                sink.success(event);
            }
        }).subscribeOn(detectLanguageScheduler).cast(WikiRecord.class);
    }

    public Flux<String> getWikis() {
        return wikis.asFlux();//.mergeWith(getDummyWikies());
    }

    public Flux<String> getTitles(String wiki) {
        if(!titlesMap.containsKey(wiki)) {
            titlesMap.put(wiki, Sinks.many().multicast().directBestEffort());
        }

        return titlesMap.get(wiki).asFlux();
    }

    public Flux<WikiRecord> getEvents() {
        return wikiClient.get().uri("/mediawiki.recentchange")
                .retrieve().bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<WikiRecord>>() {})
                .mapNotNull(ServerSentEvent::data)
                .retryWhen(Retry.indefinitely());
    }

    public Flux<String> getDummyWikies() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(duration -> "dummy " + duration).next().flux();
    }
}
