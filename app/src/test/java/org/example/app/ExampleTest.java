package org.example.app;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Paths;

public class ExampleTest {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            // Launch Chromium (or .firefox() / .webkit())
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)); // Set to true for CI/CD

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // Navigate to a website
            System.out.println("Navigating to Playwright site...");
            page.navigate("https://playwright.dev");

            // Interact with the page
            System.out.println("Page Title: " + page.title());

            // Click the 'Get Started' link
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Get started")).click();

            // Take a screenshot of the page
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("screenshot.png")));

            System.out.println("Screenshot saved as screenshot.png");

            // Close resources
            browser.close();
        }
    }
}
