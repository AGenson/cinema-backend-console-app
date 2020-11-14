package com.agenson.cinema.security;

import com.agenson.cinema.user.UserConstants;
import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SecurityIntegrationTests implements UserConstants {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityService securityService;

    private UserDB defaultUser;

    @BeforeEach
    public void setup() {
        UserDB user = new UserDB(NORMAL_USERNAME, this.encoder.encode(NORMAL_PASSWORD));

        this.defaultUser = userRepository.save(user);
    }

    @AfterEach
    public void logout() {
        this.securityService.logout();
    }

    @Test
    public void login_ShouldReturnUserDetails_WhenGivenCredentials() {
        SecurityService.UserDetails actual = this.securityService.login(NORMAL_USERNAME, NORMAL_PASSWORD);
        SecurityService.UserDetails expected = new SecurityService.UserDetails(
                this.defaultUser.getUuid(),
                this.defaultUser.getUsername(),
                this.defaultUser.getRole()
        );

        assertThat(actual).isEqualTo(expected);

        assertThat(this.securityService.getCurrentUser().isPresent()).isTrue();
        assertThat(actual).isEqualTo(this.securityService.getCurrentUser().get());
    }

    @Test
    public void login_ShouldThrowSecurityException_WhenGivenInvalidCredentials() {
        for (String username : Arrays.asList(null, UNKNOWN_USERNAME))
            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> this.securityService.login(username, NORMAL_PASSWORD))
                    .withMessage(SecurityException.Type.CONNECTION.toString());

        for (String password : Arrays.asList(null, UNKNOWN_PASSWORD))
            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> this.securityService.login(NORMAL_USERNAME, password))
                    .withMessage(SecurityException.Type.CONNECTION.toString());

        assertThat(this.securityService.getCurrentUser().isPresent()).isFalse();
    }
}
