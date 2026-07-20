package com.mechsync.modules.servicereports.application.dto;

import java.util.Objects;

public record GeneratedServiceReportPdf(String filename, byte[] content) {

    public GeneratedServiceReportPdf {
        Objects.requireNonNull(filename, "filename is required");
        Objects.requireNonNull(content, "content is required");
        content = content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
