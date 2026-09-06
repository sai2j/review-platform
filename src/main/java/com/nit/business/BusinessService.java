package com.nit.business;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class BusinessService {
	public final BusinessRepository businessRepository;

	public BusinessService(BusinessRepository businessRepository) {
		super();
		this.businessRepository = businessRepository;
	}
	public Business saveBusiness(Business business) {
		return businessRepository.save(business);
	}
	public List<Business> getAllBusinesses() {
		return businessRepository.findAll();
	}
	public Business getBusinessById(Long id) {
		return businessRepository.findById(id).orElse(null);
	}
	public void deleteBusiness(Long id) {
		businessRepository.deleteById(id);
	}
}
