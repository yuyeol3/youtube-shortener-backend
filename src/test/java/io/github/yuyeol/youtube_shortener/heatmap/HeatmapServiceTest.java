package io.github.yuyeol.youtube_shortener.heatmap;

import io.github.yuyeol.youtube_shortener.exception_handling.BusinessException;
import io.github.yuyeol.youtube_shortener.exception_handling.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeatmapServiceTest {

    @Mock
    private HeatmapParser heatmapParser;

    @Mock
    private YoutubeClient youtubeClient;

    @Mock
    private HeatmapRepository heatmapRepository;

    @Mock
    private HeatmapHistoryService heatmapHistoryService;

    @InjectMocks
    private HeatmapService heatmapService;

    @Test
    void getHeatMapByUrl_null_throwsBusinessException() {
        BusinessException ex = assertThrows(BusinessException.class, () -> heatmapService.getHeatMapByUrl(null));
        assertEquals(ErrorCode.InvalidYoutubeURL.getMessage(), ex.getMessage());
        verifyNoInteractions(heatmapRepository, youtubeClient, heatmapParser);
    }

    @Test
    void getHeatMapByUrl_empty_throwsBusinessException() {
        BusinessException ex = assertThrows(BusinessException.class, () -> heatmapService.getHeatMapByUrl(""));
        assertEquals(ErrorCode.InvalidYoutubeURL.getMessage(), ex.getMessage());
        verifyNoInteractions(heatmapRepository, youtubeClient, heatmapParser);
    }

    @Test
    void getHeatMapByUrl_invalidHost_throwsBusinessException() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> heatmapService.getHeatMapByUrl("https://vimeo.com/abc")
        );
        assertEquals(ErrorCode.InvalidYoutubeURL.getMessage(), ex.getMessage());
        verifyNoInteractions(heatmapRepository, youtubeClient, heatmapParser);
    }

    @Test
    void getHeatMapByUrl_noQueryParams_throwsBusinessException() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> heatmapService.getHeatMapByUrl("https://www.youtube.com/watch")
        );
        assertEquals(ErrorCode.InvalidYoutubeURL.getMessage(), ex.getMessage());
        verifyNoInteractions(heatmapRepository, youtubeClient, heatmapParser);
    }

    @Test
    void getHeatMapByUrl_missingVidParam_throwsBusinessException() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> heatmapService.getHeatMapByUrl("https://www.youtube.com/watch?list=PL123")
        );
        assertEquals(ErrorCode.InvalidYoutubeURL.getMessage(), ex.getMessage());
        verifyNoInteractions(heatmapRepository, youtubeClient, heatmapParser);
    }

    @Test
    void getHeatMapByUrl_emptyShortLinkId_throwsBusinessException() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> heatmapService.getHeatMapByUrl("https://youtu.be/")
        );
        assertEquals(ErrorCode.InvalidYoutubeURL.getMessage(), ex.getMessage());
        verifyNoInteractions(heatmapRepository, youtubeClient, heatmapParser);
    }

    @Test
    void getHeatMapByUrl_malformedUrl_throwsIllegalArgumentException() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> heatmapService.getHeatMapByUrl("http:/bad-url")
        );
        assertEquals(ErrorCode.InvalidYoutubeURL.getMessage(), ex.getMessage());
        verifyNoInteractions(heatmapRepository, youtubeClient, heatmapParser);
    }

    @Test
    void getHeatMapByUrl_cacheHit_returnsDtoWithoutFetching() {
        Heatmap heatmap = Mockito.spy(new Heatmap("abc123", "title", "{}"));
        when(heatmapRepository.findByVidId("abc123")).thenReturn(Optional.of(heatmap));

        HeatmapDto dto = heatmapService.getHeatMapByUrl("https://www.youtube.com/watch?v=abc123");

        verify(heatmapHistoryService).updateLastAccessedAt("abc123");
        verify(youtubeClient, never()).fetchHtml(any());
        verify(heatmapParser, never()).parseToHeatmap(any());
        verify(heatmapRepository, never()).save(any());
        assertEquals("abc123", dto.vidId());
        assertEquals("title", dto.title());
        assertNotNull(dto.heatmap());
    }

    @Test
    void getHeatMapByUrl_cacheMiss_parsesAndSaves() {
        when(heatmapRepository.findByVidId("abc123")).thenReturn(Optional.empty());

        YoutubeResponseDto response = new YoutubeResponseDto("https://www.youtube.com/watch?v=abc123", "abc123", "<html></html>");
        when(youtubeClient.fetchHtml("abc123")).thenReturn(response);

        Heatmap parsed = new Heatmap("abc123", "title", "{}");
        when(heatmapParser.parseToHeatmap(response)).thenReturn(Optional.of(parsed));

        HeatmapDto dto = heatmapService.getHeatMapByUrl("https://youtu.be/abc123");

        verify(heatmapRepository).findByVidId("abc123");
        verify(heatmapRepository).saveAndFlush(parsed);
        assertEquals("abc123", dto.vidId());
        assertEquals("title", dto.title());
    }

    @Test
    void getHeatMapByUrl_parseFailure_throwsBusinessException() {
        when(heatmapRepository.findByVidId("abc123")).thenReturn(Optional.empty());

        YoutubeResponseDto response = new YoutubeResponseDto("https://www.youtube.com/watch?v=abc123", "abc123", "<html></html>");
        when(youtubeClient.fetchHtml("abc123")).thenReturn(response);
        when(heatmapParser.parseToHeatmap(response)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> heatmapService.getHeatMapByUrl("https://www.youtube.com/watch?v=abc123")
        );
        assertEquals(ErrorCode.CouldNotParseHeatMap.getMessage(), ex.getMessage());
        verify(heatmapRepository, never()).save(any());
    }

    @Test
    void getRecentlyAccessedHeatmaps_mapsDtos() {
        List<Heatmap> heatmaps = List.of(
                new Heatmap("a1", "title1", "{}"),
                new Heatmap("b2", "title2", "{}")
        );
        when(heatmapRepository.findTop20ByOrderByLastAccessedAtDesc()).thenReturn(heatmaps);

        List<HeatmapDto> result = heatmapService.getRecentlyAccessedHeatmaps();

        assertEquals(2, result.size());
        assertEquals("a1", result.get(0).vidId());
        assertEquals("b2", result.get(1).vidId());
    }
}
