package ru.softlab.efr.infrastructure.testapp.someservice.controllers;

/**
 * @author krenev
 * @since 19.04.2017
 */
public class UploadedFileInfo {

    private long size;
    private String contentType;
    private String name;
    private String originalFilename;

    public UploadedFileInfo() {

    }

    public UploadedFileInfo(long size, String contentType, String name, String originalFilename) {
        this.size = size;
        this.contentType = contentType;
        this.name = name;
        this.originalFilename = originalFilename;
    }

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
}
