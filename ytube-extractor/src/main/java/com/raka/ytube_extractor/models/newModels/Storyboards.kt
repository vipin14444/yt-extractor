package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class Storyboards {
    @SerializedName("playerStoryboardSpecRenderer")
    var playerStoryboardSpecRenderer: PlayerStoryboardSpecRenderer? = null
    override fun toString(): String {
        return "Storyboards{" +
                "playerStoryboardSpecRenderer = '" + playerStoryboardSpecRenderer + '\'' +
                "}"
    }
}