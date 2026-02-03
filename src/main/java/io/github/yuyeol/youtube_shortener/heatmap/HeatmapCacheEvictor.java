package io.github.yuyeol.youtube_shortener.heatmap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
public class HeatmapCacheEvictor {

    private final HeatmapRepository heatmapRepository;

    public HeatmapCacheEvictor(HeatmapRepository heatmapRepository) {
        this.heatmapRepository = heatmapRepository;
    }

    // 매일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void evictExpiredCache() {
        // "수정된 지(updated_at)" 또는 "생성된 지(created_at)" 7일 지난 데이터 삭제
        // 즉, 7일 동안 아무도 갱신 안 했거나, 만들어진 지 오래된 건 버림
        LocalDateTime expirationTime = LocalDateTime.now().minusDays(7);
        heatmapRepository.deleteByCreatedAtBefore(expirationTime);
        log.info("Expired cache eviction done");
    }
}