package ru.vigilov.simple_crawler.services;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.java.Log;
import ru.vigilov.simple_crawler.Application;
import ru.vigilov.simple_crawler.helpers.Site;

import java.net.URL;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ivan on 03.09.2016.
 *
 */
@Log
public class SiteHandler {

    private final int scanDelayStep;

    @Getter
    private Site site;

    @Getter
    private AtomicInteger scanDelay;

    @Getter
    private AtomicLong lastVisit;

    @Getter
    private Set<URL> visitedPages;

    @Getter
    private Queue<URL> scanPageQueue;

    public SiteHandler(Site site) {
        this.site = site;
        scanDelay = new AtomicInteger(
                Integer.parseInt(Application.getConfig().getProperty("default_scan_delay", "100"))
        );
        scanDelayStep = Integer.parseInt(Application.getConfig().getProperty("scan_delay_step", "100"));
        visitedPages = Sets.newConcurrentHashSet();
        scanPageQueue = new ConcurrentLinkedQueue<>();
        lastVisit = new AtomicLong();
    }

    public void increaseScanDelay() {
        scanDelay.addAndGet(scanDelayStep);
    }

    public void addPageForScan(URL page) {
        if (!isVisitedPage(page) && isValidPage(page)) {
            scanPageQueue.add(page);

            if (Application.isDebug()) {
                System.out.println(
                        String.format(
                                "Page was added: %s, Sites in queue: %d, Sites added: %d",
                                page.toString(),
                                scanPageQueue.size(),
                                visitedPages.size()
                                )
                );
            }
        }
    }

    public URL getPageForScan() {
        return scanPageQueue.poll();
    }

    public void addScannedPage(URL page) {
        visitedPages.add(page);
        System.out.println("Page added: " + page.toString() + ", Sites in queue: " + scanPageQueue.size());
    }

    public boolean isVisitedPage(URL page) {
        return visitedPages.contains(page) || scanPageQueue.contains(page);
    }

    public boolean isValidPage(URL page) {
        return Objects.equals(page.getHost().toLowerCase(), site.getHost().toLowerCase());
    }
}
