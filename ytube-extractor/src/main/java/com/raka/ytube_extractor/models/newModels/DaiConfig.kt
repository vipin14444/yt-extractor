package com.raka.ytube_extractor.models.newModels

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
class DaiConfig : Parcelable, Serializable {
    @SerializedName("enableServerStitchedDai")
    var isEnableServerStitchedDai = false
    override fun toString(): String {
        return "DaiConfig{" +
                "enableServerStitchedDai = '" + isEnableServerStitchedDai + '\'' +
                "}"
    }
}