package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class UiActions {
    @SerializedName("hideEnclosingContainer")
    var isHideEnclosingContainer = false
    override fun toString(): String {
        return "UiActions{" +
                "hideEnclosingContainer = '" + isHideEnclosingContainer + '\'' +
                "}"
    }
}