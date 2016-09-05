package ru.vigilov.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Ivan on 04.09.2016.
 */
@AllArgsConstructor
public class Response {

    @Getter
    private int status;

    @Getter
    private String body;
}
