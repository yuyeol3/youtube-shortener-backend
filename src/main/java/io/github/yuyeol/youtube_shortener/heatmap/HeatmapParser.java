package io.github.yuyeol.youtube_shortener.heatmap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HeatmapParser {
    private final Logger logger = LoggerFactory.getLogger(HeatmapParser.class);
    private static final Pattern YT_INITIAL_DATA_PATTERN =
            Pattern.compile("ytInitialData\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);

    private final ObjectMapper objectMapper;

    public HeatmapParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<Heatmap> parseToHeatmap(YoutubeResponseDto response) {
        try {
            Document dom = Jsoup.parse(response.body());
            Optional<JsonNode> json = parseYoutubeInitialData(dom);

            if (json.isEmpty())
                return Optional.empty();


            Optional<JsonNode> markersList = markersList = parseMarkersList(json.get());
            Optional<String> title = parseTitle(json.get());

            if (markersList.isEmpty() ||  title.isEmpty())
                return Optional.empty();


            return Optional.of(new Heatmap(response.vidId(), title.get(), markersList.get().toString()));
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return Optional.empty();
        }

    }

    private Optional<String> parseTitle(JsonNode json) {
        JsonNode videoPrimaryInfoRenderer = json.findValue("videoPrimaryInfoRenderer");

        if (videoPrimaryInfoRenderer == null || videoPrimaryInfoRenderer.isMissingNode()) {
            return Optional.empty();
        }

        // 2. 찾은 노드 안에서 markersList를 꺼냅니다.
        JsonNode titleNode = videoPrimaryInfoRenderer
                .path("title")
                .path("runs")
                .path(0)
                .path("text");

        if (titleNode.isMissingNode()) {
            return Optional.empty();
        }

        return Optional.of(titleNode.asString());
    }

    private Optional<JsonNode> parseMarkersList(JsonNode json) {
        JsonNode macroMarkersEntity = json.findValue("macroMarkersListEntity");

        if (macroMarkersEntity == null || macroMarkersEntity.isMissingNode()) {
            return Optional.empty();
        }

        // 2. 찾은 노드 안에서 markersList를 꺼냅니다.
        JsonNode markersList = macroMarkersEntity.path("markersList");

        if (markersList.isMissingNode()) {
            return Optional.empty();
        }

        return Optional.of(markersList);
    }

    private Optional<JsonNode> parseYoutubeInitialData(Document dom) {
        Elements scripts = dom.select("script");

        for (int i = scripts.size() - 1; i >= 0; i--) {
            Element script = scripts.get(i);
            String content = script.html();
            if (content == null || content.isBlank()) continue;

            Matcher m = YT_INITIAL_DATA_PATTERN.matcher(content);
            if (!m.find()) continue;

            String jsonText = m.group(1); // "{ ... }"
            try {
                return Optional.of(objectMapper.readTree(jsonText));
            } catch (Exception ignored) {
                // 파싱 실패하면 다음 script로
            }
        }

        return Optional.empty();
    }

}
