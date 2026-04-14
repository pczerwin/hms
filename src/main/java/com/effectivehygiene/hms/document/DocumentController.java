package com.effectivehygiene.hms.document;


import com.effectivehygiene.hms.document.dto.CreateDocumentReferenceRequest;
import com.effectivehygiene.hms.document.dto.CreateDocumentVersionRequest;
import com.effectivehygiene.hms.document.dto.DocumentMapper;
import com.effectivehygiene.hms.document.dto.DocumentReferenceResponse;
import com.effectivehygiene.hms.document.dto.DocumentVersionResponse;
import jakarta.validation.Valid;
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
    }

    // --------------------
    // Document reference
    // CREATE DOCUMENT REFERENCE
    // --------------------

    @PostMapping("/references")
    public ResponseEntity<DocumentReferenceResponse> createDocumentReference(
            @Valid @RequestBody CreateDocumentReferenceRequest request
    ) {
        DocumentReference newReference = documentService.createReference(DocumentMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentMapper.toResponse(newReference));
    }


    // --------------------
    // Document reference
    // LIST ALL ACTIVE REFERENCES
    // --------------------

    @GetMapping("/references")
    public ResponseEntity<List<DocumentReferenceResponse>> getAllActiveDocumentReferences() {
        return ResponseEntity.ok(DocumentMapper.toReferenceResponseList(documentService.getAllActiveDocumentReferences()));
    }



    // --------------------
    // Document version
    // CREATE DOCUMENT VERSION
    // --------------------

    @PostMapping("/references/{referenceId}/versions")
    public ResponseEntity<DocumentVersionResponse> createDocumentVersion(
            @PathVariable Long referenceId,
            @Valid @RequestBody CreateDocumentVersionRequest request
    ) {
        DocumentVersion createdVersion =
                documentService.createVersion(referenceId, DocumentMapper.toEntity(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentMapper.toResponse(createdVersion));
    }

    // --------------------
    // LIST ALL VERSIONS FOR A DOCUMENT REFERENCE
    // --------------------

    @GetMapping("/references/{referenceId}/versions")
    public ResponseEntity<List<DocumentVersionResponse>> getAllVersionsForReference(
            @PathVariable Long referenceId
    ) {
        return ResponseEntity.ok(DocumentMapper.toVersionResponseList(documentService.getAllVersions(referenceId)));
    }

    // --------------------
    // GET CURRENT VERSION FOR A DOCUMENT REFERENCE
    // --------------------

    @GetMapping("/references/{referenceId}/versions/current")
    public ResponseEntity<DocumentVersionResponse> getCurrentVersion(
            @PathVariable Long referenceId
    ) {
        return ResponseEntity.ok(DocumentMapper.toResponse(documentService.getCurrentVersion(referenceId)));
    }
}
