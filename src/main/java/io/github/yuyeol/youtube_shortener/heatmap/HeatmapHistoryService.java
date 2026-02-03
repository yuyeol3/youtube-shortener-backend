package io.github.yuyeol.youtube_shortener.heatmap;


import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class HeatmapHistoryService {

    private final HeatmapRepository heatmapRepository;

    public HeatmapHistoryService(HeatmapRepository heatmapRepository) {
        this.heatmapRepository = heatmapRepository;
    }

    @Async
    @Transactional
    public void updateLastAccessedAt(String vidId) {
        heatmapRepository.updateLastAccessedAtByVidId(vidId);
    }


}
