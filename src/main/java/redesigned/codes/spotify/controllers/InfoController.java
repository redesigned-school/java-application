package redesigned.codes.spotify.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redesigned.codes.spotify.SpotifyService;
import xyz.gianlu.librespot.core.SearchManager;

@RestController
@RequestMapping("/info")
public class InfoController {
    @Autowired
    private SpotifyService spotify;

    @GetMapping("/search")
    public Object GetInfo(@RequestParam String query) throws Exception{
        return new SearchManager(spotify.UserSession).request(new SearchManager.SearchRequest(query));
    }
}
