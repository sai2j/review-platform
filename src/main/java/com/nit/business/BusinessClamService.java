package com.nit.business;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class BusinessClamService {

	private final BusinessclaimRepository businessClaimRepository;
	
	public BusinessClamService(BusinessclaimRepository businessClaimRepository) {
		this.businessClaimRepository=businessClaimRepository;
	}
	

	public BusinessClaim saveBusinessClaim(BusinessClaim businessClaim) {
		return businessClaimRepository.save(businessClaim);
	}

	public List<BusinessClaim> getAllBusinessClaims() {
		return businessClaimRepository.findAll();
	}

	public BusinessClaim getBusinessClaimById(Long id) {
		return businessClaimRepository.findById(id).orElse(null);
	}

	public void deleteBusinessClaim(Long id) {
		businessClaimRepository.deleteById(id);
	}
}
