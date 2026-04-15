package com.effectivehygiene.hms.document.dto;

import com.effectivehygiene.hms.document.DocumentReference;
import com.effectivehygiene.hms.document.DocumentVersion;

import java.util.List;

public final class DocumentMapper {

    private DocumentMapper() {
    }

    public static DocumentReference toEntity(CreateDocumentReferenceRequest request) {
        DocumentReference reference = new DocumentReference();
        reference.setReferenceCode(normalizeRequiredText(request.getReferenceCode()));
        reference.setOriginDepartment(normalizeRequiredText(request.getOriginDepartment()));
        reference.setMandatory(Boolean.TRUE.equals(request.getMandatory()));
        return reference;
    }

    public static DocumentVersion toEntity(CreateDocumentVersionRequest request) {
        DocumentVersion version = new DocumentVersion();
        version.setDocumentName(normalizeRequiredText(request.getDocumentName()));
        version.setVersion(normalizeRequiredText(request.getVersion()));
        version.setVersionIssueDate(request.getVersionIssueDate());
        version.setCurrent(Boolean.TRUE.equals(request.getIsCurrent()));
        version.setDefaultTrainingValidityDays(request.getDefaultTrainingValidityDays());
        return version;
    }

    public static DocumentReferenceResponse toResponse(DocumentReference reference) {
        return new DocumentReferenceResponse(
                reference.getId(),
                reference.getReferenceCode(),
                reference.getOriginDepartment(),
                reference.isMandatory(),
                reference.isActive(),
                reference.getCreatedAt()
        );
    }

    public static DocumentVersionResponse toResponse(DocumentVersion version) {
        return new DocumentVersionResponse(
                version.getId(),
                version.getDocumentReference() != null ? version.getDocumentReference().getId() : null,
                version.getDocumentName(),
                version.getVersion(),
                version.getVersionIssueDate(),
                version.isCurrent(),
                version.getDefaultTrainingValidityDays(),
                version.getCreatedAt()
        );
    }

    public static List<DocumentReferenceResponse> toReferenceResponseList(List<DocumentReference> references) {
        return references.stream().map(DocumentMapper::toResponse).toList();
    }

    public static List<DocumentVersionResponse> toVersionResponseList(List<DocumentVersion> versions) {
        return versions.stream().map(DocumentMapper::toResponse).toList();
    }

    private static String normalizeRequiredText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }
}

