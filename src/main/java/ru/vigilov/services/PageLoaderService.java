package ru.vigilov.services;

import lombok.Getter;
import lombok.extern.java.Log;
import ru.vigilov.Application;
import ru.vigilov.models.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Ivan on 04.09.2016.
 */
@Log
public class PageLoaderService {

    private static final int connectionTimeout = Integer.parseInt(
            Application.getConfig().getProperty("http_connection_timeout", "30000")
    );

    private static final int readTimeout = Integer.parseInt(
            Application.getConfig().getProperty("http_read_timeout", "30000")
    );

    /*
    * Пул потоков для выполнения IO с длительными блокировками ожидания
    * */
    @Getter
    private final ThreadPoolExecutor executor;

    public PageLoaderService() {
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    /*
    * Пытается загрузить контент страницы и вернуть исполнение
    * */
    public void loadAndHandle(URL url, ResponseCallback callback) {
        executor.execute(() -> {

            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(connectionTimeout);
                conn.setReadTimeout(readTimeout);

                int status = conn.getResponseCode();

                if (status >= 300 || status < 200) {
                    callback.execute(
                            new Response(status, null)
                    );
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String body = reader.lines().collect(Collectors.joining("\n"));

                Response response = new Response(
                        status,
                        body
                );
                callback.execute(response);
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        });
    }
}
