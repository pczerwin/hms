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
    // CREATE DOCUMENT REFERENCE
    // --------------------

    @PostMapping("/references")
    public ResponseEntity<DocumentReference> createDocumentReference(@RequestBody DocumentReference documentReference) {
        DocumentReference newReference = documentService.createReference(documentReference);
        return ResponseEntity.status(HttpStatus.CREATED).body(newReference);
    }


    // --------------------
    // LIST ALL ACTIVE REFERENCES
    // --------------------

    @GetMapping("/references")
    public ResponseEntity<List<DocumentReference>> getAllActiveDocumentReferences() {
        return ResponseEntity.ok(documentService.getAllActiveDocumentReferences());
    }


}
