package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class WikiRecord {
    private Long id;

    private String title;

    private String comment;

    @JsonProperty("server_name")
    private String serverName;

    private String wiki;

    private String user;

    private WikiMeta meta;

    private String content;

    private String language;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public WikiMeta getMeta() {
        return meta;
    }

    public void setMeta(WikiMeta meta) {
        this.meta = meta;
    }

    public String getWiki() {
        return wiki;
    }

    public void setWiki(String wiki) {
        this.wiki = wiki;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public static class WikiMeta {
        private URI uri;

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }
    }
}
