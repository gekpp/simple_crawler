package ru.vigilov.simple_crawler.utils;

import lombok.extern.java.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ivan on 04.09.2016.
 */
@Log
public class HtmlLinksParser {

    private static final Pattern HREF_PATTERN = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=\"([^\"]*)\"");

    public static Set<URL> parse(URL parentUrl, String htmlBody) {
        return parseHref(parentUrl, htmlBody);
    }

    public static Set<URL> parseHref(URL parentUrl, String htmlBody) {

        Set<URL> result = new HashSet<>();

        Matcher matcher = HREF_PATTERN.matcher(htmlBody);

        while (matcher.find()) {
            String link = matcher.group(1);
            try {
                WebURL webURL = new WebURL(parentUrl, link);

                result.add(webURL.getURL());
            } catch (MalformedURLException e) {
                log.warning(e.getMessage());
            }
        }

        return result;
    }
}
