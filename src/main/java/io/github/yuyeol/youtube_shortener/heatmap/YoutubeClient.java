package io.github.yuyeol.youtube_shortener.heatmap;


import io.github.yuyeol.youtube_shortener.exception_handling.BusinessException;
import io.github.yuyeol.youtube_shortener.exception_handling.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class YoutubeClient {
    private final RestClient restClient;
    private final String YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v=";

    public YoutubeClient() {
        this.restClient = RestClient.create();
    }


    public YoutubeResponseDto fetchHtml(String urlText) {
        String vidId = getVidId(urlText);
        if (vidId == null) {
            throw new BusinessException(ErrorCode.InvalidYoutubeURL);
        }

        String fetchURL = YOUTUBE_WATCH_URL + vidId;
        String html = restClient.get()
                .uri(fetchURL)
                .retrieve()
                .body(String.class);
        return new YoutubeResponseDto(fetchURL, vidId, html);


    }

    public String getVidId(String urlText) {
        URI uri = URI.create(urlText);
        String urlHost = uri.getHost();

        if (urlHost.equals("www.youtube.com") || urlHost.equals("youtube.com")) {
            String paramsRaw = uri.getQuery();
            String[] paramKeyValues =  paramsRaw.split("&");
            Map<String, String> params = new HashMap<>();
            for (String paramKeyValue : paramKeyValues) {
                String[] keyValue = paramKeyValue.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
            return params.get("v");
        }

        if (urlHost.equals("youtu.be")) {
            return uri.getPath().replace("/", "");
        }

        return null;
    }
}
