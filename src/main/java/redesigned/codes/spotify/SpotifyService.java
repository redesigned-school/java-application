package redesigned.codes.spotify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import com.spotify.metadata.Metadata;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.gianlu.librespot.audio.HaltListener;
import xyz.gianlu.librespot.audio.MetadataWrapper;
import xyz.gianlu.librespot.audio.PlayableContentFeeder;
import xyz.gianlu.librespot.audio.format.AudioQualityPicker;
import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.metadata.PlayableId;

@Service
public class SpotifyService {
    @Value("${ogg2mp3.path}")
    private String ogg2mp3Path;
    @Value("${spotify.temppath}")
    private String spotifyTempPath;
    private String spotifyUsername;
    private String spotifyPassword;
    public Session UserSession = null;
    public SpotifyService(@Value("${spotify.username}")String spotifyUsername, @Value("${spotify.password}")String spotifyPassword) {
        this.spotifyUsername = spotifyUsername;
        this.spotifyPassword = spotifyPassword;
        initialize();
    }
    public Boolean initialize(){
        try{
            UserSession = new Session.Builder().userPass(spotifyUsername, spotifyPassword).create();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public String[] getMusic(String track, int bitrate) throws Exception {
        PlayableContentFeeder.LoadedStream feed = UserSession.contentFeeder().load(PlayableId.fromUri("spotify:track:"+track), new Picker(), true, new HaltListener() {@Override
            public void streamReadHalted(int i, long l) {
            }

            @Override
            public void streamReadResumed(int i, long l) {

            }
        });
        byte[] data = feed.in.stream().readAllBytes();
        File output = File.createTempFile("rede",".ogg",new File(spotifyTempPath));
        System.out.println(output.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(output);
        fos.write(data);
        fos.flush();
        fos.close();
        MetadataWrapper metadata = feed.metadata;
        Process p = Runtime.getRuntime().exec(new String[]{ogg2mp3Path, output.getAbsolutePath(), Integer.toString(bitrate)}); //todo get application.properties
        synchronized (p){ //프로세스 대기 오브젝트 락
            try{
                p.waitFor(); //this dose trick
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String newPath = Path.of(output.getParent(), FilenameUtils.removeExtension(output.getName()) + ".mp3").toString();

        String url = "https://i.scdn.co/image/" + HexUtils.toHexString(metadata.getCoverImage().getImageOrBuilder(0).getFileId().toByteArray());
        AudioFile audio = AudioFileIO.read(new File(newPath));
        Tag tag = audio.getTagOrCreateDefault();
        tag.setField(FieldKey.TITLE, metadata.getName());
        tag.setField(FieldKey.ALBUM, metadata.getAlbumName());
        tag.setField(FieldKey.ARTIST, metadata.getArtist());
        tag.setField(FieldKey.ARTISTS, metadata.track.getArtistList().stream().map(x-> x.getName()).collect(Collectors.joining(", ")));
        tag.setField(FieldKey.ENCODER, "Designed");
        tag.setField(FieldKey.COPYRIGHT, "spotify:track:"+track);

        File artFile = Download(url);
        Artwork art = ArtworkFactory.createArtworkFromFile(artFile);
        tag.setField(art);
        audio.commit();
        AudioFileIO.write(audio);
        artFile.delete();
        output.delete();
        System.out.println("Metadata write complete");
        return new String[] {newPath, metadata.getName() + ".mp3"};
    }

//    public String[] getPlaylist(String playlistId, int bitrate) throws Exception {
//
//        PlayableContentFeeder.LoadedStream feed = UserSession.api().getExtendedMetadata()
//        PlayableContentFeeder.LoadedStream feed = UserSession.contentFeeder().load(PlayableId.fromUri("spotify:track:"+track), new Picker(), true, new HaltListener() {
//            @Override
//            public void streamReadHalted(int i, long l) {
//            }
//
//            @Override
//            public void streamReadResumed(int i, long l) {
//
//            }
//        });
//        byte[] data = feed.in.stream().readAllBytes();
//        File output = File.createTempFile("rede",".ogg",new File("C:\\SpotifyTemp"));
//        System.out.println(output.getAbsolutePath());
//        FileOutputStream fos = new FileOutputStream(output);
//        fos.write(data);
//        fos.flush();
//        fos.close();
//        MetadataWrapper metadata = feed.metadata;
//        Process p = Runtime.getRuntime().exec(new String[]{ogg2mp3Path, output.getAbsolutePath(), Integer.toString(bitrate)}); //todo get application.property
//        synchronized (p){
//            try{
//                p.waitFor(); //this dose trick
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        String newPath = Path.of(output.getParent(), FilenameUtils.removeExtension(output.getName()) + ".mp3").toString();
//
//        String url = "https://i.scdn.co/image/" + HexUtils.toHexString(metadata.getCoverImage().getImageOrBuilder(0).getFileId().toByteArray());
//        AudioFile audio = AudioFileIO.read(new File(newPath));
//        Tag tag = audio.getTagOrCreateDefault();
//        tag.setField(FieldKey.TITLE, metadata.getName());
//        tag.setField(FieldKey.ALBUM, metadata.getAlbumName());
//        tag.setField(FieldKey.ARTIST, metadata.getArtist());
//        tag.setField(FieldKey.ARTISTS, metadata.track.getArtistList().stream().map(x-> x.getName()).collect(Collectors.joining(", ")));
//        tag.setField(FieldKey.ENCODER, "Designed");
//        tag.setField(FieldKey.COPYRIGHT, "spotify:track:"+track);
//
//        File artFile = Download(url);
//        Artwork art = ArtworkFactory.createArtworkFromFile(artFile);
//        tag.setField(art);
//        audio.commit();
//        AudioFileIO.write(audio);
//        artFile.delete();
//        System.out.println("Metadata write complete");
//        return new String[] {newPath, metadata.getName() + ".mp3"};
//    }

    public File Download(String url) throws IOException {
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        File output = File.createTempFile("rede",".png",new File(spotifyTempPath));
        FileOutputStream fos = new FileOutputStream(output);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.flush();
        fos.close();
        return output;
    }
    public class Picker implements AudioQualityPicker {

        @Override
        public Metadata.@Nullable AudioFile getFile(@NotNull List<Metadata.AudioFile> list) {
            return list.stream().filter(x-> x.getFormat().equals(Metadata.AudioFile.Format.OGG_VORBIS_320)).findFirst().get();
        }
    }
}
