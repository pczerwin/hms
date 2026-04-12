package com.effectivehygiene.hms.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentReferenceRepository
        extends JpaRepository<DocumentReference, Long> {

    Optional<DocumentReference> findByReferenceCode(String referenceCode);

    boolean existsByReferenceCode(String referenceCode);

    List<DocumentReference> findByActiveTrue();
}

