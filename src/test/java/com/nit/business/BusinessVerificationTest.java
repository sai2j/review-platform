package com.nit.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BusinessVerificationTest {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private BusinessRepository businessRepository;

    private Long testBusinessId;

    @AfterEach
    void cleanup() {

        if (testBusinessId != null) {
            businessRepository.deleteById(testBusinessId);
        }
    }

    @Test
    void shouldVerifyBusiness() {

        // Create temporary business
        Business business = new Business();

        business.setName("Verification Test Business");
        business.setDescription(
                "Business created for verification test"
        );
        business.setOfficialUrl(
                "https://verification-test-" 
                + System.currentTimeMillis()
                + ".example.com"
        );
        business.setStatus("PENDING");

        Business savedBusiness =
                businessRepository.save(business);

        testBusinessId = savedBusiness.getId();

        assertNotNull(
                savedBusiness.getId(),
                "Business ID should be generated"
        );

        // Verify business
        Business verifiedBusiness =
                businessService.verifyBusiness(
                        savedBusiness.getId()
                );

        // Verify result
        assertNotNull(
                verifiedBusiness,
                "Verified business should not be null"
        );

        assertEquals(
                savedBusiness.getId(),
                verifiedBusiness.getId()
        );

        assertEquals(
                "VERIFIED",
                verifiedBusiness.getStatus()
        );

        // Reload from database
        Business databaseBusiness =
                businessRepository
                        .findById(savedBusiness.getId())
                        .orElse(null);

        assertNotNull(
                databaseBusiness,
                "Business should exist in database"
        );

        assertEquals(
                "VERIFIED",
                databaseBusiness.getStatus()
        );
    }
}