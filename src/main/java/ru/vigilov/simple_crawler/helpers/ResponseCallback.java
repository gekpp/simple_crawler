package ru.vigilov.simple_crawler.helpers;

import ru.vigilov.simple_crawler.helpers.Response;

/**
 * Ivan on 04.09.2016.
 */
public interface ResponseCallback {

    void execute(Response response);
}
