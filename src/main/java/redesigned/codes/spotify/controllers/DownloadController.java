package redesigned.codes.spotify.controllers;

import jakarta.servlet.http.HttpServletResponse;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.PropertyMatches;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redesigned.codes.spotify.SpotifyService;
import xyz.gianlu.librespot.audio.PlayableContentFeeder;
import xyz.gianlu.librespot.audio.cdn.CdnManager;
import xyz.gianlu.librespot.mercury.MercuryClient;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/download")
public class DownloadController {

    @Autowired
    public SpotifyService service;

    private static final Pattern pattern = Pattern.compile("(https?:\\/\\/open.spotify.com\\/(track|user|artist|album)\\/([a-zA-Z0-9]+)(\\/playlist\\/[a-zA-Z0-9]+|)|spotify:(track|user|artist|album):([a-zA-Z0-9]+)(:playlist:[a-zA-Z0-9]+|))");

    @GetMapping("/{track}")
    public ResponseEntity<Object> getTrack(@PathVariable("track")String url, @RequestParam(value = "bitrate", required = false, defaultValue = "128000")int bitrate) throws Exception {
        Matcher matcher = pattern.matcher(url);
        matcher.find();
        String track = matcher.group(6);


        String[] result = service.getMusic(track, bitrate);
        return Process(result[0],result[1]);
    }

    @Nullable
    private ResponseEntity<Object> Process(String path, String name){
        try{
            Path p = Path.of(path);
            byte[] file = Files.readAllBytes(p);
            Files.delete(p);
            return ResponseEntity.ok()
                    .contentLength(file.length)
                    .contentType(MediaType.parseMediaType("audio/mp3"))
                    .header("Content-Disposition", "attachment; filename=\""+ URLEncoder.encode(name, "UTF-8") +"\"")
                    .body(file);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
