package ru.vigilov.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URL;

/**
 * Ivan on 03.09.2016.
 */
@EqualsAndHashCode(of = {"host"})
public class Site {

    @Getter
    private String host;

    public Site(URL siteUrl) {
        this.host = siteUrl.getHost();
    }
}
