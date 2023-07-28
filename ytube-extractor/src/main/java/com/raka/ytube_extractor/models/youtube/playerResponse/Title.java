package com.raka.ytube_extractor.models.youtube.playerResponse;

import java.io.Serializable;

public class Title implements Serializable {
    private String simpleText;

    public String getSimpleText() {
        return simpleText;
    }

    public void setSimpleText(String simpleText) {
        this.simpleText = simpleText;
    }

    @Override
    public String toString() {
        return
                "Title{" +
                        "simpleText = '" + simpleText + '\'' +
                        "}";
    }
}
