package com.effectivehygiene.hms.document;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Query("""
            SELECT DISTINCT dv
            FROM DocumentVersion dv
            JOIN FETCH dv.documentReference dr
            WHERE dv.id IN :ids
            """)
    List<DocumentVersion> findAllByIdInWithReference(@Param("ids") Set<Long> ids);
}

