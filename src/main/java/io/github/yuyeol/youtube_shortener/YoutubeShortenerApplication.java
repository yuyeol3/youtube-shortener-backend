package io.github.yuyeol.youtube_shortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class YoutubeShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoutubeShortenerApplication.class, args);
    }

}
