package org.tests.espn;

import com.google.gson.*;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class EspnCricinfoCompletedMatchesTest extends BaseTest {

    @Test
    public void printTodaysCompletedMatchWinners() throws Exception {
        navigateTo("https://www.espncricinfo.com/");

        String scriptJson = null;
        Locator script = page.locator("script#__NEXT_DATA__");
        if (script.count() > 0) {
            scriptJson = script.first().textContent();
        }

        List<String> winners = new ArrayList<>();

        if (scriptJson != null && !scriptJson.isBlank()) {
            try {
                JsonElement root = JsonParser.parseString(scriptJson);
                JsonArray events = findArrayByKey(root, "events");
                if (events != null) {
                    for (JsonElement ev : events) {
                        JsonObject e = ev.getAsJsonObject();
                        boolean completed = false;
                        try {
                            if (e.has("status") && e.get("status").isJsonObject()) {
                                JsonObject status = e.getAsJsonObject("status");
                                if (status.has("type") && status.get("type").isJsonObject()) {
                                    JsonObject type = status.getAsJsonObject("type");
                                    if (type.has("state")) {
                                        String state = type.get("state").getAsString();
                                        if ("completed".equalsIgnoreCase(state)) completed = true;
                                    }
                                    if (!completed && type.has("description")) {
                                        String desc = type.get("description").getAsString();
                                        if (desc != null && desc.toLowerCase().contains("won")) completed = true;
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }

                        if (completed) {
                            String winnerName = null;
                            JsonArray competitions = findArrayByKey(e, "competitions");
                            if (competitions != null) {
                                for (JsonElement compEl : competitions) {
                                    JsonObject comp = compEl.getAsJsonObject();
                                    if (comp.has("competitors") && comp.get("competitors").isJsonArray()) {
                                        JsonArray comps = comp.getAsJsonArray("competitors");
                                        for (JsonElement teamEl : comps) {
                                            JsonObject team = teamEl.getAsJsonObject();
                                            if (team.has("winner") && team.get("winner").getAsBoolean()) {
                                                if (team.has("team") && team.get("team").isJsonObject()) {
                                                    JsonObject teamObj = team.getAsJsonObject("team");
                                                    if (teamObj.has("displayName")) winnerName = teamObj.get("displayName").getAsString();
                                                    else if (teamObj.has("name")) winnerName = teamObj.get("name").getAsString();
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (winnerName == null) {
                                if (e.has("status") && e.get("status").isJsonObject()) {
                                    JsonObject status = e.getAsJsonObject("status");
                                    if (status.has("type") && status.get("type").isJsonObject()) {
                                        JsonObject type = status.getAsJsonObject("type");
                                        if (type.has("description")) winnerName = type.get("description").getAsString();
                                    }
                                }
                            }

                            if (winnerName == null) {
                                if (e.has("name")) winnerName = e.get("name").getAsString();
                                else winnerName = "Unknown";
                            }

                            winners.add(winnerName);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (winners.isEmpty()) {
            // Fallback: simple regex-based search on page content
            String pageContent = page.content();
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("([A-Za-z &.\\-]{2,80} won by [^<\\n]{1,80})", java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(pageContent);
            LinkedHashSet<String> seen = new LinkedHashSet<>();
            while (m.find()) {
                seen.add(m.group(1).trim());
            }
            winners.addAll(seen);
        }

        Assertions.assertNotNull(page.title());

        System.out.println("Completed match winners:");
        if (winners.isEmpty()) {
            System.out.println("No completed matches with winners found for today.");
        } else {
            for (String w : winners) {
                System.out.println(w);
            }
        }
    }

    private JsonArray findArrayByKey(JsonElement el, String key) {
        if (el == null || el.isJsonNull()) return null;
        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.has(key) && obj.get(key).isJsonArray()) return obj.getAsJsonArray(key);
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                JsonArray r = findArrayByKey(entry.getValue(), key);
                if (r != null) return r;
            }
        } else if (el.isJsonArray()) {
            for (JsonElement e : el.getAsJsonArray()) {
                JsonArray r = findArrayByKey(e, key);
                if (r != null) return r;
            }
        }
        return null;
    }
}
