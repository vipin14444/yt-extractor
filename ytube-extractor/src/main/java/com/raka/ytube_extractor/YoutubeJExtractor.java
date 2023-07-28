package com.raka.ytube_extractor;

import android.util.Log;

import com.raka.ytube_extractor.exception.ExtractionException;
import com.raka.ytube_extractor.exception.SignatureDecryptionException;
import com.raka.ytube_extractor.exception.VideoIsUnavailable;
import com.raka.ytube_extractor.exception.YoutubeRequestException;
import com.raka.ytube_extractor.models.AdaptiveAudioStream;
import com.raka.ytube_extractor.models.AdaptiveVideoStream;
import com.raka.ytube_extractor.models.newModels.AdaptiveFormatsItem;
import com.raka.ytube_extractor.models.newModels.PlayabilityStatus;
import com.raka.ytube_extractor.models.newModels.VideoPlayerConfig;
import com.raka.ytube_extractor.models.subtitles.Subtitle;
import com.raka.ytube_extractor.models.youtube.playerResponse.MuxedStream;
import com.raka.ytube_extractor.models.youtube.videoData.StreamingData;
import com.raka.ytube_extractor.network.GoogleVideoNetwork;
import com.raka.ytube_extractor.network.YoutubeNetwork;
import com.raka.ytube_extractor.utils.DecryptionUtils;
import com.raka.ytube_extractor.utils.ExtractionUtils;
import com.raka.ytube_extractor.utils.YoutubePlayerUtils;
import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import com.google.gson.Gson;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.raka.ytube_extractor.utils.CommonUtils.LogE;
import static com.raka.ytube_extractor.utils.CommonUtils.LogI;
import static com.raka.ytube_extractor.utils.CommonUtils.matchWithPatterns;
import static com.raka.ytube_extractor.utils.StringUtils.splitUrlParams;


public class YoutubeJExtractor {

    private final String TAG = getClass().getSimpleName();
    private final YoutubeNetwork youtubeNetwork;
    private final YoutubePlayerUtils youtubePlayerUtils;
    private final ExtractionUtils extractionUtils;
    private final Gson gson;
    private String videoPageHtml;

    /**
     * No-args constructor
     */
    public YoutubeJExtractor() {
        gson = new IGsonFactoryImpl().initGson();
        youtubeNetwork = new YoutubeNetwork(gson);
        youtubePlayerUtils = new YoutubePlayerUtils(youtubeNetwork);
        extractionUtils = new ExtractionUtils(youtubePlayerUtils);
    }

    /**
     * Constructs YoutubeJExtractor with custom OkHttpClient instance which allows for ex.
     * to use custom proxy to deal with region restricted video
     *
     * @param client Custom OkHttpClient instance
     */
    public YoutubeJExtractor(OkHttpClient client) {
        gson = new IGsonFactoryImpl().initGson();
        youtubeNetwork = new YoutubeNetwork(gson, client);
        youtubePlayerUtils = new YoutubePlayerUtils(youtubeNetwork);
        extractionUtils = new ExtractionUtils(youtubePlayerUtils);
    }

    public VideoPlayerConfig extract(String videoId) throws ExtractionException, YoutubeRequestException, VideoIsUnavailable {
        VideoPlayerConfig videoPlayerConfig;
        try {
            LogI(TAG, "Extracting video data from youtube page");
            videoPlayerConfig = extractVideoData(videoId);
            if (isVideoUnavailable(videoPlayerConfig)) {
                String reason = videoPlayerConfig.getPlayabilityStatus().getErrorScreen()
                        .getPlayerErrorMessageRenderer().getReason().getSimpleText();
                throw new VideoIsUnavailable("This video is unavailable, reason: " + reason);
            }
            if (streamsAreCiphered(videoPlayerConfig)) {
                LogI(TAG, "Streams are ciphered, decrypting");
                decryptYoutubeStreams(videoPlayerConfig);
            } else LogI(TAG, "Streams are not encrypted");
            sortAdaptiveStreamsByType(videoPlayerConfig.getStreamingData());
        } catch (SignatureDecryptionException e) {
            throw new ExtractionException(e);
        }
        return videoPlayerConfig;
    }

    public String getUrlForAndroidEmbeddedPlayer(String videoId) throws ExtractionException, YoutubeRequestException, VideoIsUnavailable {
        VideoPlayerConfig videoData = extract(videoId);
        return Objects.requireNonNull(videoData.getStreamingData()).getMuxedStreams().get(0).getUrl().replace("c=WEB", "c=ANDROID_EMBEDDED_PLAYER");
    }

    public String getUrl(String videoId) throws ExtractionException, YoutubeRequestException, VideoIsUnavailable {
        VideoPlayerConfig videoData = extract(videoId);
        return Objects.requireNonNull(videoData.getStreamingData()).getMuxedStreams().get(0).getUrl();
    }

    private boolean isVideoUnavailable(VideoPlayerConfig videoPlayerConfig) {
        PlayabilityStatus playabilityStatus = videoPlayerConfig.getPlayabilityStatus();
        if (playabilityStatus.getReason() != null) {
            return playabilityStatus.getStatus().equals("ERROR")
                    || playabilityStatus.getReason().equals("Video unavailable");
        } else return false;
    }


    public void sortAdaptiveStreamsByType(StreamingData streamingData) {
        List<AdaptiveVideoStream> adaptiveVideoStreams = new ArrayList<>();
        List<AdaptiveAudioStream> adaptiveAudioStreams = new ArrayList<>();

        for (AdaptiveFormatsItem adaptiveFormat : streamingData.getAdaptiveFormats()) {
            String mimeType = adaptiveFormat.getMimeType();
            if (adaptiveFormat.getApproxDurationMs() == null) {
                continue;
            }
            if (mimeType.contains("audio")) {
                adaptiveAudioStreams.add(new AdaptiveAudioStream(adaptiveFormat));
            } else if (mimeType.contains("video")) {
                adaptiveVideoStreams.add(new AdaptiveVideoStream(adaptiveFormat));
            } else {
                LogE(getClass().getSimpleName(), "Unknown stream type found: " + mimeType);
            }
        }
        streamingData.setAdaptiveAudioStreams(adaptiveAudioStreams);
        streamingData.setAdaptiveVideoStreams(adaptiveVideoStreams);
    }

    public void extract(String videoId, JExtractorCallback callback) {
        try {
            VideoPlayerConfig playerResponse = extractVideoData(videoId);
            callback.onSuccess(playerResponse);
        } catch (SignatureDecryptionException | ExtractionException e) {
            callback.onError(e);
        } catch (YoutubeRequestException e) {
            callback.onNetworkException(e);
        }
    }

    public Map<String, ArrayList<Subtitle>> extractSubtitles(String videoId) {
        Response<ResponseBody> subtitlesLangsResponse;
        GoogleVideoNetwork googleVideoNetwork = new GoogleVideoNetwork(gson);
        try {
            subtitlesLangsResponse = googleVideoNetwork.getSubtitlesList(videoId);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document languagesXml = builder.parse(subtitlesLangsResponse.body().byteStream());
            NodeList languagesNodeList = languagesXml.getDocumentElement().getChildNodes();
            if (languagesNodeList.getLength() > 0) {
                ArrayList<String> availableSubtitlesLangCodes = new ArrayList<>();
                for (int i = 0; i < languagesNodeList.getLength(); i++) {
                    String langCode = languagesNodeList.item(i).getAttributes().getNamedItem("lang_code").getNodeValue();
                    availableSubtitlesLangCodes.add(langCode);
                }
                Map<String, ArrayList<Subtitle>> subtitlesByLang = new HashMap<>();
                for (String langCode : availableSubtitlesLangCodes) {
                    Response<ResponseBody> response = googleVideoNetwork.getSubtitles(videoId, langCode);
                    Document subtitlesXml = builder.parse(response.body().byteStream());
                    NodeList subLineNodeList = subtitlesXml.getDocumentElement().getChildNodes();
                    ArrayList<Subtitle> subtitleArrayList = new ArrayList<>();
                    for (int i = 0; i < subLineNodeList.getLength(); i++) {
                        Node node = subLineNodeList.item(i);
                        String start = node.getAttributes().getNamedItem("start").getNodeValue();
                        String duration = node.getAttributes().getNamedItem("dur").getNodeValue();
                        String text = node.getTextContent();
                        subtitleArrayList.add(new Subtitle(start, duration, text));
                    }
                    subtitlesByLang.put(langCode, subtitleArrayList);
                }
                return subtitlesByLang;
            } else {
                LogI(TAG, "Subtitles not found");
                return Collections.emptyMap();
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    private VideoPlayerConfig extractVideoData(String videoId) throws ExtractionException, YoutubeRequestException, SignatureDecryptionException {
        LogI(TAG, "Extracting video data from youtube page");
        return extractYoutubeVideoData(videoId);
    }

    private VideoPlayerConfig extractYoutubeVideoData(String videoId) throws ExtractionException, YoutubeRequestException {
        VideoPlayerConfig playerResponse;
        try {
            URL url;
            videoPageHtml = youtubeNetwork.getYoutubeVideoPage(videoId).body().string();
            //Protocol and domain are necessary to split url params correctly
            String urlProtocolAndDomain = "http://youtube.con/v?";
            if (extractionUtils.isVideoAgeRestricted(videoPageHtml)) {
                LogI(TAG, "Age restricted video detected, getting video data from google apis");
                String videoInfo = getVideoInfoForAgeRestrictedVideo(videoId);
                url = new URL(urlProtocolAndDomain + videoInfo);
                Map<String, String> videoInfoMap = splitUrlParams(url);
                String rawPlayerResponse = videoInfoMap.get("player_response");
                if (rawPlayerResponse == null || rawPlayerResponse.isEmpty()) {
                    throw new ExtractionException("Player response extracted from video info was null or empty");
                }

                //TODO: Check if this works
                playerResponse = gson.fromJson(rawPlayerResponse, VideoPlayerConfig.class);
            } else {
                LogI(TAG, "Video is not age restricted, extracting youtube video player config");
                playerResponse = extractYoutubePlayerConfig(videoId);
            }
        } catch (IOException e) {
            throw new ExtractionException(e);
        }
        return playerResponse;
    }

    private VideoPlayerConfig extractYoutubePlayerConfig(String videoId) throws ExtractionException {
        List<Pattern> patterns = Arrays.asList(
                Pattern.compile("ytInitialPlayerResponse\\s*=\\s*(\\{.+?\\})\\s*;"),
                Pattern.compile(";ytplayer\\.config\\s*=\\s*(\\{.+?\\});ytplayer"),
                Pattern.compile(";ytplayer\\.config\\s*=\\s*(\\{.+?\\});")
        );
        String result = matchWithPatterns(patterns, videoPageHtml);
        if (result != null) {
            return gson.fromJson(result, VideoPlayerConfig.class);
        } else {
            Pattern videoIsUnavailableMessagePattern = Pattern.compile("<h1\\sid=\"unavailable-message\"\\sclass=\"message\">\\n\\s+(.+?)\\n\\s+<\\/h1>");
            Matcher matcher = videoIsUnavailableMessagePattern.matcher(videoPageHtml);
            if (matcher.find()) {
                throw new ExtractionException(String.format("Cannot extract youtube player config, " +
                        "videoId was: %s, reason: %s", videoId, matcher.group(1)));
            } else
                throw new ExtractionException("Cannot extract youtube player config, videoId was: " + videoId);
        }
    }

    private String getVideoInfoForAgeRestrictedVideo(String videoId) throws ExtractionException {
        try {
            this.videoPageHtml = youtubeNetwork.getYoutubeEmbeddedVideoPage(videoId).body().string();
            String sts = extractionUtils.extractStsFromVideoPageHtml(videoPageHtml);
            String eUrl = String.format("https://youtube.googleapis.com/v/%s&sts=%s", videoId, sts);
            Response<ResponseBody> videoInfoResponse = youtubeNetwork.getYoutubeVideoInfo(videoId, eUrl);
            if (videoInfoResponse.body() != null) {
                String videoInfo = videoInfoResponse.body().string();
                if (videoInfo.isEmpty())
                    throw new ExtractionException("Video info was empty");
                else return videoInfo;
            } else {
                throw new ExtractionException("Video info response body was null or empty");
            }
        } catch (IOException | NullPointerException | YoutubeRequestException e) {
            throw new ExtractionException(e);
        }
    }

    private boolean streamsAreCiphered(VideoPlayerConfig videoPlayerConfig) throws ExtractionException {
        // Even if a single stream is encrypted it means they all are
        StreamingData streamingData = videoPlayerConfig.getStreamingData();
        if (streamingData != null) {
            List<AdaptiveFormatsItem> formatItems = streamingData.getAdaptiveFormats();
            if (videoPlayerConfig.getVideoDetails().isLiveContent()) {
                Log.i(TAG, "Requested content is live stream");
                if (formatItems == null || formatItems.size() == 0) {
                    Log.i(TAG, "Requested content is a live stream and doesn't contain adaptive streams, " +
                            "use DASH or HLS manifests. If the content is not a live stream or it was but has ended, " +
                            "just wait some time, youtube usually needs a couple of hours to prepare adaptive streams");
                    return false;
                }
            }
            if (formatItems != null && formatItems.size() > 0) {
                return formatItems.get(0).getCipher() != null;
            } else
                throw new ExtractionException("AdaptiveFormatItem list was null or empty");
        } else throw new ExtractionException("RawStreamingData object was null");
    }

    private void decryptYoutubeStreams(VideoPlayerConfig playerConfig) throws ExtractionException, SignatureDecryptionException, YoutubeRequestException {
        List<AdaptiveFormatsItem> adaptiveStreams = playerConfig.getStreamingData().getAdaptiveFormats();
        List<MuxedStream> muxedStreams = playerConfig.getStreamingData().getMuxedStreams();

        String playerUrl = youtubePlayerUtils.getJsPlayerUrl(videoPageHtml);
        String youtubeVideoPlayerCode = extractionUtils.extractYoutubeVideoPlayerCode(playerUrl);
        String decryptFunctionName = extractionUtils.extractDecryptFunctionName(youtubeVideoPlayerCode);
        DecryptionUtils decryptionUtils = new DecryptionUtils(youtubeVideoPlayerCode, decryptFunctionName);
        for (int i = 0; i < adaptiveStreams.size(); i++) {
            AdaptiveFormatsItem adaptiveStream = adaptiveStreams.get(i);
            String encryptedSignature = adaptiveStream.getCipher().getS();
            String decryptedSignature = decryptionUtils.decryptSignature(encryptedSignature);
            adaptiveStream.getCipher().setS(decryptedSignature);
        }
        for (int i = 0; i < muxedStreams.size(); i++) {
            MuxedStream muxedStream = muxedStreams.get(i);
            String encryptedSignature = muxedStream.getCipher().getS();
            String decryptedSignature = decryptionUtils.decryptSignature(encryptedSignature);
            muxedStream.getCipher().setS(decryptedSignature);
        }
    }
}