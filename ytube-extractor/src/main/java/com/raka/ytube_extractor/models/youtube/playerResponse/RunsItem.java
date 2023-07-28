package com.raka.ytube_extractor.models.youtube.playerResponse;

import java.io.Serializable;

public class RunsItem implements Serializable {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return
                "RunsItem{" +
                        "text = '" + text + '\'' +
                        "}";
    }
}
