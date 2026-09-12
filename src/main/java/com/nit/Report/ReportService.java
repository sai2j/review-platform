package com.nit.Report;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.nit.user.User;
import com.nit.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    public ReportService(
            ReportRepository reportRepository,
            UserRepository userRepository) {

        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public Report saveReport(Report report) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // Do not trust userId from frontend
        report.setUserId(loggedInUser.getId());

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