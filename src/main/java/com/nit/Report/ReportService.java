package com.nit.Report;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report saveReport(Report report) {
        if (report.getStatus() == null || report.getStatus().isBlank()) {
            report.setStatus("PENDING");
        }

        return reportRepository.save(report);
    }

    public List<Report> getallReports() {
        return reportRepository.findAll();
    }

    public Report getReportById(Long id) {
        return reportRepository.findById(id).orElse(null);
    }

    public Report updateReportStatus(Long id, String status) {

        Report report = reportRepository.findById(id).orElse(null);

        if (report == null) {
            return null;
        }

        report.setStatus(status);

        return reportRepository.save(report);
    }

    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
}