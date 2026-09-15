package com.nit.complaint;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.review.ReviewRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository) {

        this.complaintRepository = complaintRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public Complaint createComplaint(
            Long reviewId,
            String topic,
            String description) {

        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found");
        }

        User loggedInUser = getLoggedInUser();

        Complaint complaint = new Complaint(
                reviewId,
                loggedInUser.getId(),
                topic,
                description
        );

        return complaintRepository.save(complaint);
    }

    public List<Complaint> getComplaintsByReviewId(Long reviewId) {

        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found");
        }

        return complaintRepository.findByReviewId(reviewId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public Complaint getComplaintById(Long id) {

        return complaintRepository
                .findById(id)
                .orElse(null);
    }

    public Complaint updateComplaintStatus(
            Long id,
            String status,
            String resolution) {

        Complaint complaint = complaintRepository
                .findById(id)
                .orElse(null);

        if (complaint == null) {
            return null;
        }

        complaint.setStatus(status);
        complaint.setResolution(resolution);

        return complaintRepository.save(complaint);
    }

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "You must be logged in");
        }

        User user =
                userRepository.findByEmail(
                        authentication.getName());

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found");
        }

        return user;
    }
}