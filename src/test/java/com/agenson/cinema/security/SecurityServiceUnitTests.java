package com.agenson.cinema.security;

import com.agenson.cinema.user.UserConstants;
import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.agenson.cinema.security.SecurityService.UserDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceUnitTests implements UserConstants {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityService securityService;

    private UserDB defaultUser;

    @BeforeEach
    public void setup() {
        String encodedPassword = ENCODER.encode(NORMAL_PASSWORD);

        this.defaultUser = new UserDB(NORMAL_USERNAME, encodedPassword);

        lenient().when(this.encoder.matches(NORMAL_PASSWORD, encodedPassword)).thenReturn(true);
        lenient().when(this.encoder.matches(UNKNOWN_PASSWORD, encodedPassword)).thenReturn(false);
        when(this.userRepository.findByUsername(NORMAL_USERNAME)).thenReturn(Optional.of(this.defaultUser));
    }

    @Test
    public void login_ShouldReturnUserDetails_WhenGivenCredentials() {
        UserDetails actual = this.securityService.login(NORMAL_USERNAME, NORMAL_PASSWORD);
        UserDetails expected = new UserDetails(
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
        when(this.userRepository.findByUsername(UNKNOWN_USERNAME)).thenReturn(Optional.empty());
        when(this.userRepository.findByUsername(null)).thenReturn(Optional.empty());

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

    @Test
    public void isLoggedIn_ShouldReturnWhetherAUserIsLoggedIn() {
        assertThat(this.securityService.isLoggedIn()).isFalse();

        this.securityService.login(NORMAL_USERNAME, NORMAL_PASSWORD);

        assertThat(this.securityService.isLoggedIn()).isTrue();
    }

    @Test
    public void hasRole_ShouldReturnWhetherCurrentUserHasRole_WhenGivenSecurityRole() {
        assertThat(this.securityService.hasRole(SecurityRole.STAFF)).isFalse();

        this.securityService.login(NORMAL_USERNAME, NORMAL_PASSWORD);

        assertThat(this.securityService.hasRole(SecurityRole.STAFF)).isFalse();

        this.defaultUser.setRole(SecurityRole.STAFF);
        this.securityService.login(NORMAL_USERNAME, NORMAL_PASSWORD);

        assertThat(this.securityService.hasRole(SecurityRole.STAFF)).isTrue();
    }

    @Test
    public void isUser_ShouldReturnWhetherCurrentUserMatches_WhenGivenUserUuid() {
        assertThat(this.securityService.isUser(this.defaultUser.getUuid())).isFalse();

        this.securityService.login(NORMAL_USERNAME, NORMAL_PASSWORD);

        assertThat(this.securityService.isUser(this.defaultUser.getUuid())).isTrue();
        assertThat(this.securityService.isUser(UUID.randomUUID())).isFalse();
    }

    @Test
    public void logout_ShouldSetCurrentUserToNull() {
        this.securityService.login(NORMAL_USERNAME, NORMAL_PASSWORD);

        assertThat(this.securityService.getCurrentUser().isPresent()).isTrue();

        this.securityService.logout();

        assertThat(this.securityService.getCurrentUser().isPresent()).isFalse();
    }
}
