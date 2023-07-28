package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class Text {
    @SerializedName("runs")
    var runs: List<RunsItem>? = null
    override fun toString(): String {
        return "Text{" +
                "runs = '" + runs + '\'' +
                "}"
    }
}