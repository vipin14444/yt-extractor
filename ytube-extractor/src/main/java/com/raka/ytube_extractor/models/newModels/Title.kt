package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class Title {
    @SerializedName("simpleText")
    var simpleText: String? = null
    override fun toString(): String {
        return "Title{" +
                "simpleText = '" + simpleText + '\'' +
                "}"
    }
}