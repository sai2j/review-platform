package com.nit.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nit.user.User;
import com.nit.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class BusinessClamServiceTest {

    @Mock
    private BusinessclaimRepository businessClaimRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    private BusinessClamService businessClamService;

    @BeforeEach
    void setUp() {

        businessClamService = new BusinessClamService(
                businessClaimRepository,
                userRepository,
                businessRepository
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null
                )
        );
    }

    @Test
    void shouldCreateBusinessClaimAsPending() {

        User user = new User();
        user.setId(10L);
        user.setEmail("test@example.com");

        BusinessClaim claim = new BusinessClaim();
        claim.setBusinessId(100L);
        claim.setUserId(999L);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(user);

        when(businessRepository.existsById(100L))
                .thenReturn(true);

        when(businessClaimRepository.save(any(BusinessClaim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessClaim result =
                businessClamService.saveBusinessClaim(claim);

        assertEquals(10L, result.getUserId());
        assertEquals(100L, result.getBusinessId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void shouldRejectClaimWhenBusinessDoesNotExist() {

        User user = new User();
        user.setId(10L);
        user.setEmail("test@example.com");

        BusinessClaim claim = new BusinessClaim();
        claim.setBusinessId(100L);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(user);

        when(businessRepository.existsById(100L))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> businessClamService.saveBusinessClaim(claim)
                );

        assertEquals(
                "Business does not exist",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectClaimWhenUserDoesNotExist() {

        BusinessClaim claim = new BusinessClaim();
        claim.setBusinessId(100L);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(null);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> businessClamService.saveBusinessClaim(claim)
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );
    }
}