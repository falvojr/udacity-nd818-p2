package com.falvojr.nd818.p2.model;

import com.google.gson.annotations.SerializedName;

/**
 * Config images from TMDb API.
 * <p>
 * Created by falvojr on 6/4/17.
 */
public class ConfigImages {

    @SerializedName("base_url")
    private String baseUrl;

    @SerializedName("secure_base_url")
    private String secureBaseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSecureBaseUrl() {
        return secureBaseUrl;
    }

    public void setSecureBaseUrl(String secureBaseUrl) {
        this.secureBaseUrl = secureBaseUrl;
    }
}
