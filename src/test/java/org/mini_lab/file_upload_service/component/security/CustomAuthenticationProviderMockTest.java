package org.mini_lab.file_upload_service.component.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.PASSWORD_HASH;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.RAW_PASSWORD;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationProviderMockTest {

    @InjectMocks
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void authenticate_whenUserDetailsLoadedAndPasswordMatched_thenReturnUsernamePasswordAuthenticationToken() {
        UserDetails userDetails = createUserDetails();
        Authentication authenticationRequest = createAuthenticationRequest();

        when(userDetailsService.loadUserByUsername(NORMALIZED_USERNAME))
                .thenReturn(userDetails);

        when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH))
                .thenReturn(true);

        Authentication result =
                customAuthenticationProvider.authenticate(authenticationRequest);

        assertNotNull(result);
        assertInstanceOf(
                UsernamePasswordAuthenticationToken.class,
                result
        );
        assertTrue(result.isAuthenticated());
        assertEquals(NORMALIZED_USERNAME, result.getPrincipal());
        assertNull(result.getCredentials());

        verify(userDetailsService).loadUserByUsername(NORMALIZED_USERNAME);
        verify(passwordEncoder).matches(
                RAW_PASSWORD,
                PASSWORD_HASH
        );
    }

    @Test
    void authenticate_whenUsernameNotFound_thenThrowUsernameNotFoundException() {
        Authentication authenticationRequest = createAuthenticationRequest();

        when(userDetailsService.loadUserByUsername(NORMALIZED_USERNAME))
                .thenThrow(
                        new UsernameNotFoundException(
                                ErrorCode.USER_NOT_FOUND.getDefaultMessage()
                        )
                );

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customAuthenticationProvider.authenticate(
                        authenticationRequest
                )
        );

        assertEquals(
                ErrorCode.USER_NOT_FOUND.getDefaultMessage(),
                exception.getMessage()
        );

        verify(userDetailsService).loadUserByUsername(NORMALIZED_USERNAME);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void authenticate_whenUserDetailsLoadedButPasswordNotMatches_thenThrowBadCredentialsException() {
        UserDetails userDetails = createUserDetails();
        Authentication authenticationRequest = createAuthenticationRequest();

        when(userDetailsService.loadUserByUsername(NORMALIZED_USERNAME))
                .thenReturn(userDetails);

        when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH))
                .thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> customAuthenticationProvider.authenticate(
                        authenticationRequest
                )
        );

        verify(userDetailsService).loadUserByUsername(NORMALIZED_USERNAME);
        verify(passwordEncoder).matches(
                RAW_PASSWORD,
                PASSWORD_HASH
        );
    }

    private Authentication createAuthenticationRequest() {
        return UsernamePasswordAuthenticationToken.unauthenticated(
                NORMALIZED_USERNAME,
                RAW_PASSWORD
        );
    }

    private UserDetails createUserDetails() {
        return User.builder()
                .username(NORMALIZED_USERNAME)
                .password(PASSWORD_HASH)
                .build();
    }
}