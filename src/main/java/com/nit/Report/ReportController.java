package com.nit.Report;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Logged-in users can create reports
    @PostMapping
    public Report createReport(@Valid @RequestBody Report report) {
        return reportService.saveReport(report);
    }

    // CREATE REPORT FOR REVIEW - PDF API
    @PostMapping("/review/{reviewId}")
    public Report createReportForReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody Report report) {

        report.setReviewId(reviewId);

        return reportService.saveReport(report);
    }

    // Only ADMIN can view all reports
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Report> getAllReports() {
        return reportService.getallReports();
    }

    // Only ADMIN can view a report
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Report getReportById(@PathVariable Long id) {
        return reportService.getReportById(id);
    }

    // Only ADMIN can change report status
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.PutMapping("/{id}/status")
    public Report updateReportStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return reportService.updateReportStatus(id, status);
    }

    // Only ADMIN can delete reports
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return "Report deleted successfully";
    }
}