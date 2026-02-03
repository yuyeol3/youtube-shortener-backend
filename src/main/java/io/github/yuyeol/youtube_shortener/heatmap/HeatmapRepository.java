package io.github.yuyeol.youtube_shortener.heatmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HeatmapRepository extends JpaRepository<Heatmap, Long> {

    void deleteByCreatedAtBefore(LocalDateTime createdAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Heatmap set lastAccessedAt = CURRENT_TIMESTAMP where vidId = :vidId")
    void updateLastAccessedAtByVidId(@Param("vidId") String vidId);

    List<Heatmap> findTop20ByOrderByLastAccessedAtDesc();

    Optional<Heatmap> findByVidId(String vidId);
}
