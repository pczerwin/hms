package com.effectivehygiene.hms.document;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 2️⃣ Core business rules (locked)
 * These are the rules we must encode now, not later:
 *
 * A DocumentReference must exist to create a version
 * A reference can have many versions
 * Exactly one version may be marked as current
 * Creating a new “current” version:
 *
 * unsets the previous current version
 *
 *
 * Document versions are immutable
 *
 * no updates
 * no deletes
 *
 *
 * References are soft-deletable
 * Version issue date is business-provided, never inferred
 */
@Service
@Transactional
public class DocumentService {

    private final DocumentReferenceRepository referenceRepository;
    private final DocumentVersionRepository versionRepository;

    public DocumentService(
            DocumentReferenceRepository referenceRepository,
            DocumentVersionRepository versionRepository
    ) {
        this.referenceRepository = referenceRepository;
        this.versionRepository = versionRepository;
    }

    // --------------------------------------------------
    // Create reference
    // Create document in Document Reference. Throw exception if reference code exists
    // --------------------------------------------------

    public DocumentReference createReference(DocumentReference reference) {
        if (referenceRepository.existsByReferenceCode(reference.getReferenceCode())) {
            throw new IllegalStateException(
                    "Document reference already exists: " + reference.getReferenceCode()
            );
        }
        return referenceRepository.save(reference);
    }

    // --------------------------------------------------
    // Create version
    // Throw exception if reference does not exists
    // If reference is NOT active, reactivate it for new version
    // --------------------------------------------------

    public DocumentVersion createVersion(
            Long documentReferenceId,
            DocumentVersion newVersion
    ) {
        DocumentReference parentReference = referenceRepository.findById(documentReferenceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Document reference not found: id=" + documentReferenceId
                ));

        if (!parentReference.isActive()) {
                parentReference.setActive(true);
                referenceRepository.save(parentReference);
        }

        // Enforce "only one current version"
        if (newVersion.isCurrent()) {
            versionRepository
                    .findByDocumentReferenceAndIsCurrentTrue(parentReference)
                    .ifPresent(replacedVersion -> {
                        replacedVersion.setCurrent(false);
                        versionRepository.save(replacedVersion);
                    });

        }

        newVersion.setDocumentReference(parentReference);
        return versionRepository.save(newVersion);
    }

    // --------------------------------------------------
    // Document reference
    // Find all active references
    // --------------------------------------------------

    public List<DocumentReference> getAllActiveDocumentReferences() {
        return referenceRepository.findByActiveTrue();
    }

    // --------------------------------------------------
    // Document version
    // find all versions (current + outdated) for a doc reference
    // --------------------------------------------------

    public List<DocumentVersion> getAllVersions(Long documentReferenceId) {
        DocumentReference reference = referenceRepository.findById(documentReferenceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Document reference not found: id=" + documentReferenceId
                ));

        return versionRepository
                .findByDocumentReferenceOrderByVersionIssueDateDesc(reference);
    }

    // --------------------------------------------------
    // Document version
    // Find current version for a doc reference
    // --------------------------------------------------

    public DocumentVersion getCurrentVersion(Long documentReferenceId) {
        DocumentReference reference = referenceRepository.findById(documentReferenceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Document reference not found: id=" + documentReferenceId
                ));

        return versionRepository
                .findByDocumentReferenceAndIsCurrentTrue(reference)
                .orElseThrow(() -> new IllegalStateException(
                        "No current version exists for document reference: " + documentReferenceId
                ));
    }
}
