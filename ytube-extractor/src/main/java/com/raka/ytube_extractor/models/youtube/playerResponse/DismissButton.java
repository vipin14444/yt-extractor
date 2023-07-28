package com.raka.ytube_extractor.models.youtube.playerResponse;

import java.io.Serializable;

public class DismissButton implements Serializable {

    private ButtonRenderer buttonRenderer;

    public ButtonRenderer getButtonRenderer() {
        return buttonRenderer;
    }

    public void setButtonRenderer(ButtonRenderer buttonRenderer) {
        this.buttonRenderer = buttonRenderer;
    }

    @Override
    public String toString() {
        return
                "DismissButton{" +
                        "buttonRenderer = '" + buttonRenderer + '\'' +
                        "}";
    }
}
