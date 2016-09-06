package ru.vigilov.simple_crawler.services;

import lombok.extern.java.Log;
import org.apache.http.HttpStatus;
import ru.vigilov.simple_crawler.Application;
import ru.vigilov.simple_crawler.utils.HtmlLinksParser;

import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Ivan on 04.09.2016.
 */
@Log
public class SiteScannerService {

    /*
    * Сервис для загрузки страниц сайта
    * */
    private final PageLoaderService loader;

    /*
    * Выделяем пул для активных задач,здесь будет выполнятся поиск линков на полученных страницах
    * */
    private final ThreadPoolExecutor processingPool;

    /*
    * Ограничиваем число воркеров
    * */
    private final int maxThreads;

    public SiteScannerService() {
        processingPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        loader = new PageLoaderService();

        maxThreads = Integer.parseInt(Application.getConfig().getProperty("threads_by_core", "4"));
    }

//FIXME:
/**
 * Запускает в потоке цикл загрузки и парсинга страниц. Если на вход придет много урлов, которые нужно парсить, (10К, например), 
 * то программа зависнет.
 * Попробуй избавиться от ручного управления потоками. Например, можно использовать другие пулы. 
 */
 
    /*
    * Запускает в сервисном потоке поиск загруженных страниц
    * для дальнейшего процессинга
    * */
    public void startPageScanner(final SiteHandler handler) {
        processingPool.execute(() -> {
            while (true) {

                if (checkFinish(handler)) {
                    log.info(String.format(
                            "Scanning finished (%s). Scanned Pages: %d, Pages in queue: %d",
                            handler.getSite().getHost(),
                            handler.getVisitedPages().size(),
                            handler.getScanPageQueue().size()
                    ));
                    break;
                }

                if (isOverloadThreads(
                        loader.getExecutor().getActiveCount(),
                        processingPool.getActiveCount(),
                        maxThreads
                ) || !checkScanDelay(handler)) {
                    slowDown();
                    continue;
                }
                URL scanPage = handler.getPageForScan();
                if (scanPage == null) {
                    continue;
                }

                loader.loadAndHandle(scanPage, (response) -> processingPool.execute(() -> {
                    if (response.getStatus() == HttpStatus.SC_BAD_GATEWAY ||
                            response.getStatus() == 429) {
                        handler.increaseScanDelay();
                        return;
                    } else if (response.getStatus() >= 400) {
                        log.info("Site not available");
                    } else if (response.getStatus() >= 300) {
                        log.info("Redirect not supported");
                    } else if (response.getStatus() < 200 || response.getStatus() > 200) {
                        log.info("Response not supported");
                    }

                    handler.addScannedPage(scanPage);

                    try {
                        String body = response.getBody();

                        HtmlLinksParser.parse(scanPage, body)
                                .forEach(handler::addPageForScan);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                    }
                }));
            }
        });
    }

    /*
    * Тормозит рабочий поток
    * */
    private static void slowDown() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            log.warning(e.getMessage());
        }
    }

    /*
    * Проверяет пул на перегрузку не активными потоками
    * */
    private static boolean isOverloadThreads(int waiting, int active, int maxThreads) {
        if (active == 0) {
            active = 1;
        }
        if (Application.isDebug()) {
            System.out.println(
                    String.format(
                            "Waiting threads: %d, Active threads: %d, Max threads: %d",
                            waiting,
                            active,
                            maxThreads
                    )
            );
        }

        if (active > maxThreads) {
            return true;
        }

        return waiting > 4 && active > 1 && (waiting / active) <= 4;
    }

    /*
    * Проверяем выставленную задержку
    * */
    private static boolean checkScanDelay(SiteHandler handler) {
        int scanDelay = handler.getScanDelay().get();
        if (scanDelay == 0) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long lastVisit = handler.getLastVisit().get();

        if ((currentTime - lastVisit) > scanDelay) {
            handler.getLastVisit().set(currentTime);
            return true;
        }
        return false;
    }

    /*
    * Проверяет активные задачи
    * */
    private boolean checkFinish(SiteHandler handler) {
        return handler.getScanPageQueue().size() == 0 &&
                processingPool.getActiveCount() == 1 &&
                loader.getExecutor().getActiveCount() == 0;
    }
}
