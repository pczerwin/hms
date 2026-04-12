package com.effectivehygiene.hms.document;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    };

    // --------------------
    // Document reference
    // CREATE DOCUMENT REFERENCE
    // --------------------

    @PostMapping("/references")
    public ResponseEntity<DocumentReference> createDocumentReference(@RequestBody DocumentReference documentReference) {
        DocumentReference newReference = documentService.createReference(documentReference);
        return ResponseEntity.status(HttpStatus.CREATED).body(newReference);
    }


    // --------------------
    // Document reference
    // LIST ALL ACTIVE REFERENCES
    // --------------------

    @GetMapping("/references")
    public ResponseEntity<List<DocumentReference>> getAllActiveDocumentReferences() {
        return ResponseEntity.ok(documentService.getAllActiveDocumentReferences());
    }



    // --------------------
    // Document version
    // CREATE DOCUMENT VERSION
    // --------------------

    @PostMapping("/references/{referenceId}/versions")
    public ResponseEntity<DocumentVersion> createDocumentVersion(
            @PathVariable Long referenceId,
            @RequestBody DocumentVersion documentVersion
    ) {
        DocumentVersion createdVersion =
                documentService.createVersion(referenceId, documentVersion);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
    }

    // --------------------
    // LIST ALL VERSIONS FOR A DOCUMENT REFERENCE
    // --------------------

    @GetMapping("/references/{referenceId}/versions")
    public ResponseEntity<List<DocumentVersion>> getAllVersionsForReference(
            @PathVariable Long referenceId
    ) {
        return ResponseEntity.ok(documentService.getAllVersions(referenceId));
    }

    // --------------------
    // GET CURRENT VERSION FOR A DOCUMENT REFERENCE
    // --------------------

    @GetMapping("/references/{referenceId}/versions/current")
    public ResponseEntity<DocumentVersion> getCurrentVersion(
            @PathVariable Long referenceId
    ) {
        return ResponseEntity.ok(documentService.getCurrentVersion(referenceId));
    }
}
