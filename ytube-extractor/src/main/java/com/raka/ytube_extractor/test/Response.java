package com.raka.ytube_extractor.test;

import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("args")
    private Args args;

    public Args getArgs() {
        return args;
    }

    public void setArgs(Args args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return
                "Response{" +
                        "args = '" + args + '\'' +
                        "}";
    }
}