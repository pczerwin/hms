package com.effectivehygiene.hms.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateDocumentVersionRequest {

    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Document name must be at most 255 characters")
    private String docName;

    @NotBlank(message = "Version is required")
    @Size(max = 20, message = "Version must be at most 20 characters")
    private String version;

    @NotNull(message = "Version issue date is required")
    private LocalDate versionIssueDate;

    private Boolean isCurrent;

    private Integer defaultTrainingValidityDays;

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDate getVersionIssueDate() {
        return versionIssueDate;
    }

    public void setVersionIssueDate(LocalDate versionIssueDate) {
        this.versionIssueDate = versionIssueDate;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean current) {
        isCurrent = current;
    }

    public Integer getDefaultTrainingValidityDays() {
        return defaultTrainingValidityDays;
    }

    public void setDefaultTrainingValidityDays(Integer defaultTrainingValidityDays) {
        this.defaultTrainingValidityDays = defaultTrainingValidityDays;
    }
}

