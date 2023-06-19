package redesigned.codes.spotify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redesigned.codes.spotify.controllers.DownloadController;

@SpringBootApplication
public class SpotifyBackendApplication {

    public static void main(String[] args) {
        System.out.println("boot");
        SpringApplication.run(SpotifyBackendApplication.class, args);
    }

}
