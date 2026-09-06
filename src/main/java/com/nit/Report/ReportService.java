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
		return reportRepository.save(report);
	}
	public List<Report> getallReports() {
		return reportRepository.findAll();
	}
	public Report getReportById(Long id) {
		return reportRepository.findById(id).orElse(null);
	}
	public void deleteReport(Long id) {
		reportRepository.deleteById(id);
	}
}
