package com.nit.complaint;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // =========================
    // CREATE COMPLAINT
    // LOGGED-IN USER
    // =========================

    @PostMapping
    public Complaint createComplaint(
            @RequestParam Long reviewId,
            @RequestParam String topic,
            @RequestParam(required = false) String description) {

        return complaintService.createComplaint(
                reviewId,
                topic,
                description
        );
    }

    // =========================
    // GET COMPLAINTS BY REVIEW
    // =========================

    @GetMapping("/review/{reviewId}")
    public List<Complaint> getComplaintsByReviewId(
            @PathVariable Long reviewId) {

        return complaintService.getComplaintsByReviewId(reviewId);
    }

    // =========================
    // GET ALL COMPLAINTS
    // ADMIN ONLY
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Complaint> getAllComplaints() {

        return complaintService.getAllComplaints();
    }

    // =========================
    // GET COMPLAINT BY ID
    // ADMIN ONLY
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Complaint getComplaintById(
            @PathVariable Long id) {

        return complaintService.getComplaintById(id);
    }

    // =========================
    // UPDATE STATUS + RESOLUTION
    // ADMIN ONLY
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/resolution")
    public Complaint updateComplaintResolution(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String resolution) {

        return complaintService.updateComplaintStatus(
                id,
                status,
                resolution
        );
    }
}