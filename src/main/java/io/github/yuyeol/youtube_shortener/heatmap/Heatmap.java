package io.github.yuyeol.youtube_shortener.heatmap;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "heatmap")
@Getter
public class Heatmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title", nullable = false)
    private String title;

    @Column(name="vid_id", unique = true, nullable = false)
    private String vidId;

    @Column(name="heatmap", nullable = false)
    private String heatmap;

    @Column(name="last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt = LocalDateTime.now();

    public Heatmap() {}

    public Heatmap(String vidId, String title, String heatmap) {
        this.vidId = vidId;
        this.title = title;
        this.heatmap = heatmap;
    }

    public void updateLastAccessedAt() {
        this.lastAccessedAt = LocalDateTime.now();
    }

}
