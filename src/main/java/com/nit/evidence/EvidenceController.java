package com.nit.evidence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @PostMapping(
            value = "/review/{reviewId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Evidence uploadEvidence(
            @PathVariable Long reviewId,
            @RequestParam("file") MultipartFile file) {

        return evidenceService.uploadEvidence(reviewId, file);
    }

    /*
     * =========================
     * MODERATOR / ADMIN
     * REQUEST EVIDENCE
     * =========================
     */
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @PostMapping("/review/{reviewId}/request")
    public String requestEvidence(
            @PathVariable Long reviewId) {

        return evidenceService.requestEvidence(reviewId);
    }

    @GetMapping("/review/{reviewId}")
    public List<Evidence> getEvidenceByReviewId(
            @PathVariable Long reviewId) {

        return evidenceService.getEvidenceByReviewId(reviewId);
    }

    @GetMapping("/{id}")
    public Evidence getEvidenceById(
            @PathVariable Long id) {

        return evidenceService.getEvidenceById(id);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadEvidence(
            @PathVariable Long id) {

        Path path = evidenceService.getEvidenceFile(id);

        try {

            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new RuntimeException(
                        "Evidence file cannot be read");
            }

            String contentType =
                    Files.probeContentType(path);

            if (contentType == null) {
                contentType =
                        MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + path.getFileName()
                                    + "\"")
                    .body(resource);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to read evidence file");
        }
    }
}