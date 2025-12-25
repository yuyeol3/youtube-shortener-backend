package io.github.yuyeol.youtube_shortener.heatmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

public record HeatmapDto(
        @JsonProperty("vid_id") String vidId,
        String title,
        JsonNode heatmap,
        @JsonProperty("last_accessed_at") LocalDateTime lastAccessedAt
) {

    public static HeatmapDto from(Heatmap heatmap) {
        return new HeatmapDto(
                heatmap.getVidId(),
                heatmap.getTitle(),
                new ObjectMapper().readTree(heatmap.getHeatmap()),
                heatmap.getLastAccessedAt()
        );
    }
}
