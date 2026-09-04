package com.nit.business;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class BusinessUserService {

	private final BusinessuserRepository businessUserRepository;
	

    public BusinessUserService(BusinessuserRepository businessUserRepository) {
        this.businessUserRepository = businessUserRepository;
    }

    public BusinessUser saveBusinessUser(BusinessUser businessUser) {
        return businessUserRepository.save(businessUser);
    }

    public List<BusinessUser> getAllBusinessUsers() {
        return businessUserRepository.findAll();
    }

    public BusinessUser getBusinessUserById(Long id) {
        return businessUserRepository.findById(id).orElse(null);
    }

    public void deleteBusinessUser(Long id) {
        businessUserRepository.deleteById(id);
    }
}
