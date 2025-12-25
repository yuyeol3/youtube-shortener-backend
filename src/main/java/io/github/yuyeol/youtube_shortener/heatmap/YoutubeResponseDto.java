package io.github.yuyeol.youtube_shortener.heatmap;

public record YoutubeResponseDto(
        String url,
        String vidId,
        String body
) {
}
