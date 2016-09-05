package ru.vigilov;

import lombok.Getter;
import lombok.extern.java.Log;
import ru.vigilov.models.Site;
import ru.vigilov.services.SiteHandler;
import ru.vigilov.services.SiteScannerService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Ivan on 03.09.2016.
 */
@Log
public class Application {

    @Getter
    private static Properties config;

    @Getter
    private static boolean debug;

    //    java -jar Application http://neemble.ru
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage with urls in PARAMETERS");
            return;
        }

        config = initConfig();
        debug = Boolean.parseBoolean(config.getProperty("debug", "false"));

        SiteScannerService scanner = new SiteScannerService();

        for (String stringUrl : args) {
            try {
                URL url = new URL(stringUrl);

                SiteHandler siteHandler = new SiteHandler(
                        new Site(url)
                );
                siteHandler.addPageForScan(url);

                scanner.startPageScanner(siteHandler);

                log.info(String.format("Site (%1s) was added", stringUrl));
            } catch (MalformedURLException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private static Properties initConfig() {

        Properties config = new Properties();

        InputStream input = null;
        try {
            input = new FileInputStream("src/main/resources/config.properties");
            config.load(input);
        } catch (IOException ex) {
            log.warning(ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return config;
    }
}
