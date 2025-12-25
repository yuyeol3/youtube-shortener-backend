package io.github.yuyeol.youtube_shortener.heatmap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HeatmapParser {
    private static final Pattern YT_INITIAL_DATA_PATTERN =
            Pattern.compile("ytInitialData\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);

    private final ObjectMapper objectMapper;

    public HeatmapParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<Heatmap> parseToHeatmap(YoutubeResponseDto response) {
        try {
            Document dom = Jsoup.parse(response.body());
            JsonNode json = parseYoutubeInitialData(dom);

            if (json == null) return Optional.empty();
            String markersList = parseMarkersList(json).toString();
            String title = parseTitle(json);

            return Optional.of(new Heatmap(response.vidId(), title, markersList));
        }
        catch (NullPointerException e) {
            return Optional.empty();
        }

    }

    private String parseTitle(JsonNode json) {
        return json
                .get("contents")
                .get("twoColumnWatchNextResults")
                .get("results")
                .get("results")
                .get("contents").get(0)
                .get("videoPrimaryInfoRenderer")
                .get("title")
                .get("runs").get(0)
                .get("text").asString();
    }

    private JsonNode parseMarkersList(JsonNode json) {
            return json
                    .get("frameworkUpdates")
                    .get("entityBatchUpdate")
                    .get("mutations")
                    .get(0)
                    .get("payload")
                    .get("macroMarkersListEntity")
                    .get("markersList");

    }

    private JsonNode parseYoutubeInitialData(Document dom) {
        Elements scripts = dom.select("script");

        for (int i = scripts.size() - 1; i >= 0; i--) {
            Element script = scripts.get(i);
            String content = script.html();
            if (content == null || content.isBlank()) continue;

            Matcher m = YT_INITIAL_DATA_PATTERN.matcher(content);
            if (!m.find()) continue;

            String jsonText = m.group(1); // "{ ... }"
            try {
                return objectMapper.readTree(jsonText);
            } catch (Exception ignored) {
                // 파싱 실패하면 다음 script로
            }
        }

        return null;
    }

}
