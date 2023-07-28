package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class StreamSelectionConfig {
    @SerializedName("maxBitrate")
    var maxBitrate: String? = null
    override fun toString(): String {
        return "StreamSelectionConfig{" +
                "maxBitrate = '" + maxBitrate + '\'' +
                "}"
    }
}