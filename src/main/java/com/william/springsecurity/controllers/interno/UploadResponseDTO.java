package com.william.springsecurity.controllers.interno;

public class UploadResponseDTO {
    private String url;

    public UploadResponseDTO() {}

    public UploadResponseDTO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
