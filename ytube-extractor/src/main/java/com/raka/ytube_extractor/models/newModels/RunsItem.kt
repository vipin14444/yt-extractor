package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class RunsItem {
    @SerializedName("text")
    var text: String? = null
    override fun toString(): String {
        return "RunsItem{" +
                "text = '" + text + '\'' +
                "}"
    }
}