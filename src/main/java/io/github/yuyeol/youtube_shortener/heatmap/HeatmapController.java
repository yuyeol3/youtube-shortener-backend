package io.github.yuyeol.youtube_shortener.heatmap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/heatmap")
public class HeatmapController {

    private final HeatmapService heatmapService;

    public HeatmapController(HeatmapService heatmapService) {
        this.heatmapService = heatmapService;
    }

    @GetMapping("recents")
    public ResponseEntity<List<HeatmapDto>> getHeatmap() {
        List<HeatmapDto> heatmaps = heatmapService.getRecentlyAccessedHeatmaps();
        return ResponseEntity.ok(heatmaps);
    }

    @GetMapping
    public ResponseEntity<HeatmapDto> getHeatmapByUrl(
            @RequestParam String url
    ) {
        HeatmapDto heatmap = heatmapService.getHeatMapByUrl(url);
        return ResponseEntity.ok(heatmap);
    }

}
