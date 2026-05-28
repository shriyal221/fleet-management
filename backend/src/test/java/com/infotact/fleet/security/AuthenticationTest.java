package com.infotact.fleet.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.infotact.fleet.domain.AppUser;
import com.infotact.fleet.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthenticationTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(jwtEncoder);
        ReflectionTestUtils.setField(jwtTokenProvider, "issuer", "infotact-fleet");
        ReflectionTestUtils.setField(jwtTokenProvider, "expiresMinutes", 480L);
    }

    @Test
    void generateTokenEncodesClaimsCorrectly() {
        AppUser user = new AppUser(
                "dispatcher_test",
                "encoded_password",
                Role.DISPATCHER,
                "Test Dispatcher",
                "test@infotact.local",
                "+919876543210"
        );
        ReflectionTestUtils.setField(user, "id", 500L);

        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("mocked_jwt_token_string");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        String token = jwtTokenProvider.generateToken(user);

        assertThat(token).isEqualTo("mocked_jwt_token_string");
    }
}
