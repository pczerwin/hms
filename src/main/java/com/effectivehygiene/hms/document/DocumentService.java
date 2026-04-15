package com.effectivehygiene.hms.document;

import com.effectivehygiene.hms.domain.exception.DuplicateEntityException;
import com.effectivehygiene.hms.domain.exception.EntityNotFoundException;
import com.effectivehygiene.hms.document.dto.CreateDocumentReferenceRequest;
import com.effectivehygiene.hms.domain.exception.MissingCurrentVersionException;
import com.effectivehygiene.hms.document.dto.CreateDocumentVersionRequest;
import com.effectivehygiene.hms.document.dto.DocumentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public DocumentReference createReference(CreateDocumentReferenceRequest request) {
        DocumentReference reference = DocumentMapper.toEntity(request);

        if (referenceRepository.existsByReferenceCode(reference.getReferenceCode())) {
            throw new DuplicateEntityException(
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
            CreateDocumentVersionRequest request
    ) {
        DocumentVersion newVersion = DocumentMapper.toEntity(request);

        DocumentReference parentReference = referenceRepository.findById(documentReferenceId)
                .orElseThrow(() -> new EntityNotFoundException(
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

    @Transactional(readOnly = true)
    public List<DocumentReference> getAllActiveDocumentReferences() {
        return referenceRepository.findByActiveTrue();
    }

    // --------------------------------------------------
    // Document version
    // find all versions (current + outdated) for a doc reference
    // --------------------------------------------------

    @Transactional(readOnly = true)
    public List<DocumentVersion> getAllVersions(Long documentReferenceId) {
        DocumentReference reference = referenceRepository.findById(documentReferenceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Document reference not found: id=" + documentReferenceId
                ));

        return versionRepository
                .findByDocumentReferenceOrderByVersionIssueDateDesc(reference);
    }

    // --------------------------------------------------
    // Document version
    // Find current version for a doc reference
    // --------------------------------------------------

    @Transactional(readOnly = true)
    public DocumentVersion getCurrentVersion(Long documentReferenceId) {
        DocumentReference reference = referenceRepository.findById(documentReferenceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Document reference not found: id=" + documentReferenceId
                ));

        return versionRepository
                .findByDocumentReferenceAndIsCurrentTrue(reference)
                .orElseThrow(() -> new MissingCurrentVersionException(
                        "No current version exists for document reference: " + documentReferenceId
                ));
    }
}
