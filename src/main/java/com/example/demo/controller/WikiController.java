package com.example.demo.controller;

import com.example.demo.services.WikiService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class WikiController {

    private final WikiService wikiService;

    public WikiController(WikiService wikiService) {
        this.wikiService = wikiService;
    }

    @RequestMapping(
            value = "wikis",
            produces = { MediaType.TEXT_EVENT_STREAM_VALUE },
            method = RequestMethod.GET)
    public Flux<ServerSentEvent<String>> getWikis() {
        return wikiService.getWikis().distinct()
                .map(wiki -> ServerSentEvent.builder(wiki).build());
    }

    @RequestMapping(
            value = "titles",
            produces = { MediaType.TEXT_EVENT_STREAM_VALUE },
            method = RequestMethod.GET)
    public Flux<ServerSentEvent<String>> getTitles() {
        return wikiService.getTitles("all")
                .map(title -> ServerSentEvent.builder(title).build());
    }

    @RequestMapping(
            value = "titles/{wiki}",
            produces = { MediaType.TEXT_EVENT_STREAM_VALUE },
            method = RequestMethod.GET)
    public Flux<ServerSentEvent<String>> getTitlesForWiki(@PathVariable String wiki) {
        return wikiService.getTitles(wiki)
                .map(title -> ServerSentEvent.builder(title).build());
    }
}
