package ru.softlab.efr.infrastructure.apigateway;

public class UploadedFileInfo {

    private long size;
    private String contentType;
    private String name;
    private String originalFilename;

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
}
