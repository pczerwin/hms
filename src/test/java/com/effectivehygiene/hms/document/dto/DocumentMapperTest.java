package com.effectivehygiene.hms.document.dto;

import com.effectivehygiene.hms.document.DocumentReference;
import com.effectivehygiene.hms.document.DocumentVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    @Test
    void toEntity_reference_normalizesTextAndDefaultsMandatoryFalse() {
        CreateDocumentReferenceRequest request = new CreateDocumentReferenceRequest();
        request.setReferenceCode("  SOP-001  ");
        request.setOriginDepartment("  Hygiene  ");
        request.setMandatory(null);

        DocumentReference reference = DocumentMapper.toEntity(request);

        assertThat(reference.getReferenceCode()).isEqualTo("SOP-001");
        assertThat(reference.getOriginDepartment()).isEqualTo("Hygiene");
        assertThat(reference.isMandatory()).isFalse();
    }

    @Test
    void toEntity_version_normalizesTextAndDefaultsCurrentFalse() {
        CreateDocumentVersionRequest request = new CreateDocumentVersionRequest();
        request.setDocumentName("  GMP Procedure  ");
        request.setVersion("  v1  ");
        request.setVersionIssueDate(LocalDate.of(2026, 4, 13));
        request.setIsCurrent(null);
        request.setDefaultTrainingValidityDays(365);

        DocumentVersion version = DocumentMapper.toEntity(request);

        assertThat(version.getDocumentName()).isEqualTo("GMP Procedure");
        assertThat(version.getVersion()).isEqualTo("v1");
        assertThat(version.getVersionIssueDate()).isEqualTo(LocalDate.of(2026, 4, 13));
        assertThat(version.isCurrent()).isFalse();
        assertThat(version.getDefaultTrainingValidityDays()).isEqualTo(365);
    }
}

