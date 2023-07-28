package com.raka.ytube_extractor.models.newModels

import com.google.gson.annotations.SerializedName

class ServiceTrackingParamsItem {
    @SerializedName("service")
    var service: String? = null

    @SerializedName("params")
    var params: List<ParamsItem>? = null
    override fun toString(): String {
        return "ServiceTrackingParamsItem{" +
                "service = '" + service + '\'' +
                ",params = '" + params + '\'' +
                "}"
    }
}