package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class UnsubscribeAccessibility {
    @SerializedName("accessibilityData")
    var accessibilityData: AccessibilityData? = null
    override fun toString(): String {
        return "UnsubscribeAccessibility{" +
                "accessibilityData = '" + accessibilityData + '\'' +
                "}"
    }
}