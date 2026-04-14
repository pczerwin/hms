package com.effectivehygiene.hms.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateDocumentReferenceRequest {

    @NotBlank(message = "Reference code is required")
    @Size(max = 100, message = "Reference code must be at most 100 characters")
    private String referenceCode;

    @NotBlank(message = "Origin department is required")
    @Size(max = 50, message = "Origin department must be at most 50 characters")
    private String originDepartment;

    private Boolean mandatory;

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getOriginDepartment() {
        return originDepartment;
    }

    public void setOriginDepartment(String originDepartment) {
        this.originDepartment = originDepartment;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }
}

