package com.effectivehygiene.hms.document;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository
        extends JpaRepository<DocumentVersion, Long> {

    //Find all versions
    List<DocumentVersion> findByDocumentReference(
            DocumentReference documentReference
    );
    //Find current version
    Optional<DocumentVersion> findByDocumentReferenceAndIsCurrentTrue(
            DocumentReference documentReference
    );
    //Does current version exist
    boolean existsByDocumentReferenceAndIsCurrentTrue(
            DocumentReference documentReference
    );

    // Useful later for compliance queries
    List<DocumentVersion> findByDocumentReferenceOrderByVersionIssueDateDesc(
            DocumentReference documentReference
    );
}

