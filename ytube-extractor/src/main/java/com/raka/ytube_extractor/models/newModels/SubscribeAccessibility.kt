package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class SubscribeAccessibility {
    @SerializedName("accessibilityData")
    var accessibilityData: AccessibilityData? = null
    override fun toString(): String {
        return "SubscribeAccessibility{" +
                "accessibilityData = '" + accessibilityData + '\'' +
                "}"
    }
}