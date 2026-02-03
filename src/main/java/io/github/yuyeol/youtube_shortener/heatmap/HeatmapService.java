package io.github.yuyeol.youtube_shortener.heatmap;

import io.github.yuyeol.youtube_shortener.exception_handling.BusinessException;
import io.github.yuyeol.youtube_shortener.exception_handling.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class HeatmapService {

    private final HeatmapParser heatmapParser;
    private final YoutubeClient youtubeClient;
    private final HeatmapRepository heatmapRepository;
    private final HeatmapHistoryService heatmapHistoryService;

    public HeatmapService(HeatmapParser heatmapParser,
                          YoutubeClient youtubeClient,
                          HeatmapRepository heatmapRepository,
                          HeatmapHistoryService heatmapHistoryService
    ) {
        this.heatmapParser = heatmapParser;
        this.youtubeClient = youtubeClient;
        this.heatmapRepository = heatmapRepository;
        this.heatmapHistoryService = heatmapHistoryService;
    }
    @Transactional
    public HeatmapDto getHeatMapByUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.InvalidYoutubeURL);
        }

        String vidId = getVidId(url)
                .orElseThrow(() -> new BusinessException(ErrorCode.InvalidYoutubeURL));

        Optional<Heatmap> cached = heatmapRepository.findByVidId(vidId);

        // cache hit
        if (cached.isPresent()) {
            Heatmap heatmap = cached.get();
            heatmapHistoryService.updateLastAccessedAt(vidId);
            return HeatmapDto.from(heatmap);
        }
        // cache miss
        return fetchAndSaveHeatmap(vidId);
    }

    public HeatmapDto fetchAndSaveHeatmap(String vidId) {
        YoutubeResponseDto response;
        try {
            response = youtubeClient.fetchHtml(vidId);
        }
        catch (Exception e) {
            throw new BusinessException(ErrorCode.CouldNotFetchHeatMapData);
        }

        try {
            Heatmap heatmap = heatmapParser.parseToHeatmap(response)
                    .orElseThrow(()->new BusinessException(ErrorCode.CouldNotParseHeatMap));
            heatmapRepository.saveAndFlush(heatmap);
            return HeatmapDto.from(heatmap);
        }
        catch (DataIntegrityViolationException e) {
            Heatmap heatmap = heatmapRepository.findByVidId(vidId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.InvalidYoutubeURL));
            heatmapHistoryService.updateLastAccessedAt(vidId);
            return HeatmapDto.from(heatmap);
        }

    }


    public List<HeatmapDto> getRecentlyAccessedHeatmaps() {
        List<Heatmap> heatmaps = heatmapRepository.findTop20ByOrderByLastAccessedAtDesc();
        return heatmaps.stream().map(HeatmapDto::from).toList();
    }


    private Optional<String> getVidId(String urlText) {
        URI uri = URI.create(urlText);
        String urlHost = uri.getHost();

        if (urlHost == null)
            return Optional.empty();

        if (urlHost.equals("www.youtube.com") || urlHost.equals("youtube.com")) {
            String paramsRaw = uri.getQuery();
            if (paramsRaw == null)
                return Optional.empty();

            String[] paramKeyValues =  paramsRaw.split("&");
            Map<String, String> params = new HashMap<>();
            for (String paramKeyValue : paramKeyValues) {
                String[] keyValue = paramKeyValue.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }

            String vidId = params.get("v");
            return vidId == null ? Optional.empty() : Optional.of(vidId);
        }

        if (urlHost.equals("youtu.be")) {
            String vidId = uri.getPath().replace("/", "");
            if (vidId.isEmpty())
                return Optional.empty();
            return Optional.of(vidId);
        }

        return Optional.empty();
    }
}
