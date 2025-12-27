package io.github.yuyeol.youtube_shortener.heatmap;


import io.github.yuyeol.youtube_shortener.exception_handling.BusinessException;
import io.github.yuyeol.youtube_shortener.exception_handling.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class YoutubeClient {
    private final RestClient restClient;
    private final String YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v=";

    public YoutubeClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public YoutubeResponseDto fetchHtml(String vidId) {
        String fetchURL = YOUTUBE_WATCH_URL + vidId;
        String html = restClient
                .get()
                .uri(fetchURL)
                .retrieve()
                .body(String.class);
        return new YoutubeResponseDto(fetchURL, vidId, html);

    }


}
