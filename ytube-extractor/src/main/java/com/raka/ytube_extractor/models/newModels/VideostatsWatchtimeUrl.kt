package com.raka.ytube_extractor.models.newModels

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
class VideostatsWatchtimeUrl : Parcelable, Serializable {
    @SerializedName("baseUrl")
    var baseUrl: String? = null
    override fun toString(): String {
        return "VideostatsWatchtimeUrl{" +
                "baseUrl = '" + baseUrl + '\'' +
                "}"
    }
}