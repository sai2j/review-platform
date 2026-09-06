package com.nit.business;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class BusinessResponseService {

    private final BusinessResponseRepositor businessResponseRepository;

    public BusinessResponseService(BusinessResponseRepositor businessResponseRepository) {
        this.businessResponseRepository = businessResponseRepository;
    }
    public BusinessResponse saveBusinessResponse(BusinessResponse businessResponse) {
        return businessResponseRepository.save(businessResponse);
    }
    public List<BusinessResponse> getAllBusinessResponses() {
        return businessResponseRepository.findAll();
    }
    public BusinessResponse getBusinessResponseById(Long id) {
        return businessResponseRepository.findById(id).orElse(null);
    }
    public void deleteBusinessResponse(Long id) {
        businessResponseRepository.deleteById(id);
    }
}