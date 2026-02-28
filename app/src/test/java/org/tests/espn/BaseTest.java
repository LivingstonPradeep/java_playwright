package org.tests.espn;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import java.util.Properties;

public class BaseTest {

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    // New: loaded junit platform properties from resources
    protected static Properties junitProperties = new Properties();

    @BeforeAll
    public static void globalSetup() {
        // Load junit-platform.properties from test resources (classpath)
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("junit-platform.properties")) {
            if (in != null) {
                junitProperties.load(in);
                System.out.println("Loaded junit-platform.properties: " + junitProperties);
            } else {
                System.out.println("junit-platform.properties not found on classpath");
            }
        } catch (Exception e) {
            System.out.println("Failed to load junit-platform.properties: " + e.getMessage());
        }

        // Determine headless mode: priority -> system property (-Dplaywright.headless), junit-platform.properties, default = false (non-headless)
        String headlessProp = System.getProperty("playwright.headless");
        if (headlessProp == null || headlessProp.isBlank()) {
            headlessProp = junitProperties.getProperty("playwright.headless");
        }
        if (headlessProp == null || headlessProp.isBlank()) {
            headlessProp = "false"; // default to non-headless
        }
        boolean headless = Boolean.parseBoolean(headlessProp);
        System.out.println("Playwright headless mode = " + headless + " (use -Dplaywright.headless=true to override)");

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @AfterAll
    public static void globalTeardown() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    public void setup() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    public void teardown() {
        if (page != null) {
            try { page.close(); } catch (Exception ignored) {}
        }
        if (context != null) {
            try { context.close(); } catch (Exception ignored) {}
        }
    }

    protected void navigateTo(String url) {
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    }

    // Getter for tests to access loaded junit properties
    protected static String getJunitProperty(String key) {
        return junitProperties.getProperty(key);
    }
}
