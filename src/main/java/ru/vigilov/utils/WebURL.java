package ru.vigilov.utils;

import lombok.Getter;
import lombok.extern.java.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ivan on 04.09.2016.
 */
@Log
public class WebURL {

    private static final Pattern ABS_URL_PATTERN = Pattern.compile("(http://|https://)(www.)?([a-zA-Z0-9]+)");

    @Getter
    private String host;

    @Getter
    private String protocol;

    @Getter
    private String path;

    @Getter
    private URL parentURL;

    @Getter
    private URL URL;

    public WebURL(URL parentURL, String url) throws MalformedURLException {
        if (Objects.isNull(url) || Objects.equals(url, "")) {
            return;
        }

        this.parentURL = parentURL;

        Matcher urlMatcher = ABS_URL_PATTERN.matcher(url);
        if (urlMatcher.find()) {
            URL = new URL(url);
            return;
        }

        boolean isRelativePath = true;

        if (Objects.equals(url.substring(0, 1), "/")) {
            isRelativePath = false;
        }
        host = parentURL.getHost();
        protocol = parentURL.getProtocol();
        path = url;

        createURL(isRelativePath);
    }

    private void createURL(boolean isRelativePath) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();

        builder.append(protocol);
        builder.append("://");
        builder.append(host);
        if (isRelativePath && !Objects.equals(parentURL.getPath(), "/")) {
            builder.append(parentURL.getPath());
        }
        builder.append(path);

        URL = new URL(builder.toString());
    }
}
