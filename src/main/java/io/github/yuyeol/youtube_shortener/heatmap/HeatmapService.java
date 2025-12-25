package io.github.yuyeol.youtube_shortener.heatmap;

import io.github.yuyeol.youtube_shortener.exception_handling.BusinessException;
import io.github.yuyeol.youtube_shortener.exception_handling.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HeatmapService {

    private final HeatmapParser heatmapParser;
    private final YoutubeClient youtubeClient;
    private final HeatmapRepository heatmapRepository;
    public HeatmapService(HeatmapParser heatmapParser, YoutubeClient youtubeClient, HeatmapRepository heatmapRepository) {
        this.heatmapParser = heatmapParser;
        this.youtubeClient = youtubeClient;
        this.heatmapRepository = heatmapRepository;
    }

    public HeatmapDto getHeatMapByUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new BusinessException(ErrorCode.InvalidYoutubeURL);
        }

        Optional<Heatmap> cached = heatmapRepository.findByVidId(youtubeClient.getVidId(url));

        if (cached.isPresent()) {
            Heatmap heatmap = cached.get();
            return HeatmapDto.from(heatmap);
        }

        YoutubeResponseDto response = youtubeClient.fetchHtml(url);
        Heatmap heatmap = heatmapParser.parseToHeatmap(response).orElseThrow(()->new BusinessException(ErrorCode.CouldNotParseHeatMap));
        heatmapRepository.save(heatmap);
        return HeatmapDto.from(heatmap);
    }

    public List<HeatmapDto> getRecentlyAccessedHeatmaps() {
        List<Heatmap> heatmaps = heatmapRepository.findTop20ByOrderByLastAccessedAtDesc();
        return heatmaps.stream().map(HeatmapDto::from).toList();
    }

}
