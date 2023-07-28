package com.raka.ytube_extractor;

import com.raka.ytube_extractor.exception.YoutubeRequestException;
import com.raka.ytube_extractor.models.newModels.VideoPlayerConfig;

public interface JExtractorCallback {

    void onSuccess(VideoPlayerConfig videoData);

    void onNetworkException(YoutubeRequestException e);

    void onError(Exception exception);
}
