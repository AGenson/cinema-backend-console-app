package com.agenson.cinema.user;

import com.agenson.cinema.security.UserRole;
import com.agenson.cinema.security.SecurityContext;
import com.agenson.cinema.security.SecurityException;
import com.agenson.cinema.utils.CallableOneArgument;
import com.agenson.cinema.utils.StaffSecurityAssertion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserIntegrationTests implements UserConstants {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private SecurityContext securityContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        if (this.securityContext.isLoggedIn())
            this.securityContext.logout();

        this.loginAs(null, UserRole.STAFF);
    }

    @AfterEach
    public void logout() {
        this.securityContext.logout();
    }

    @Test
    public void findUser_ShouldReturnPersistedUser_WhenGivenUuidOrUsername() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));

        UserDTO expected = this.mapper.map(user, UserDTO.class);
        Optional<UserDTO> actual = this.userService.findUser(user.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);

        actual = this.userService.findUser(user.getUsername());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findUser_ShouldReturnNull_WhenNotFoundWithUuidOrUsername() {
        assertThat(this.userService.findUser(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.userService.findUser(UNKNOWN_USERNAME).isPresent()).isFalse();
    }

    @Test
    public void findUsers_ShouldReturnUserList() {
        List<UserDB> userList = Arrays.asList(
                this.newUserInstance(NORMAL_USERNAME),
                this.newUserInstance(ANOTHER_USERNAME)
        );

        assertThat(this.userRepository.findAll().size()).isZero();

        this.userRepository.saveAll(userList);

        List<UserDTO> actual = this.userService.findUsers();
        List<UserDTO> expected = userList.stream()
                .map(user -> this.mapper.map(user, UserDTO.class))
                .collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findUsers_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.findUsers(),
                () -> this.loginAs(null, UserRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void loginUser_ShouldReturnPersistedUser_WhenGivenCredentials() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));

        UserDTO expected = this.mapper.map(user, UserDTO.class);
        UserDTO actual = this.userService.loginUser(user.getUsername(), NORMAL_PASSWORD);

        assertThat(actual).isEqualTo(expected);
        assertThat(this.securityContext.isUser(user.getUuid())).isTrue();
    }

    @Test
    public void loginUser_ShouldThrowInvalidUserException_WhenGivenInvalidCredentials() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));

        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(() -> this.userService.loginUser(UNKNOWN_USERNAME, user.getPassword()));

        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(() -> this.userService.loginUser(user.getUsername(), UNKNOWN_PASSWORD));
    }

    @Test
    public void logoutUser_ShouldLogoutCurrentUser() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user.getUuid(), user.getRole());

        assertThat(this.securityContext.isLoggedIn()).isTrue();

        this.userService.logoutUser();

        assertThat(this.securityContext.isLoggedIn()).isFalse();
        assertThat(this.securityContext.isUser(user.getUuid()));
    }

    @Test
    public void createUser_ShouldReturnPersistedUser_WhenGivenCredentials() {
        UserDTO expected = this.userService.createUser(NORMAL_USERNAME, NORMAL_PASSWORD);

        Optional<UserDB> actualDB = this.userRepository.findByUuid(expected.getUuid());
        Optional<UserDTO> actualDTO = actualDB.map(user -> this.mapper.map(user, UserDTO.class));

        assertThat(actualDTO.isPresent()).isTrue();
        assertThat(actualDTO.get()).isEqualTo(expected);

        String encodedPassword = actualDB.get().getPassword();
        assertThat(ENCODER.matches(NORMAL_PASSWORD, encodedPassword)).isTrue();
        assertThat(this.securityContext.isUser(actualDTO.get().getUuid())).isTrue();
    }

    @Test
    public void createUser_ShouldNotPersistUser_WhenGivenInvalidCredentials() {
        this.assertShouldNotPersistUser_WhenGivenInvalidUsername(username -> {
            this.userService.createUser(username, NORMAL_PASSWORD);
        });

        this.assertShouldNotPersistUser_WhenGivenInvalidPassword(password -> {
            this.userService.createUser(NORMAL_USERNAME, password);
        });
    }

    @Test
    public void updateUserUsername_ShouldReturnModifiedUser_WhenGivenUuidAndUsername() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user.getUuid(), user.getRole());

        Optional<UserDTO> expected = this.userService.updateUserUsername(user.getUuid(), ANOTHER_USERNAME);
        Optional<UserDTO> actual = this.userRepository.findByUuid(user.getUuid())
                .map(userDB -> this.mapper.map(userDB, UserDTO.class));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateUserUsername_ShouldNotPersistUser_WhenGivenInvalidUsername() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user.getUuid(), user.getRole());

        this.assertShouldNotPersistUser_WhenGivenInvalidUsername(username -> {
            this.userService.updateUserUsername(user.getUuid(), username);
        });
    }

    @Test
    public void updateUserUsername_ShouldThrowSecurityException_WhenNotCalledByUser() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.updateUserUsername(UUID.randomUUID(), UNKNOWN_USERNAME),
                () -> this.loginAs(null, UserRole.STAFF),
                () -> this.logout()
        );
    }

    @Test
    public void updateUserPassword_ShouldPersistNewPassword_WhenGivenUuidAndPassword() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user.getUuid(), user.getRole());

        this.userService.updateUserPassword(user.getUuid(), ANOTHER_PASSWORD);
        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isTrue();

        String encodedPassword = actual.get().getPassword();
        assertThat(ENCODER.matches(ANOTHER_PASSWORD, encodedPassword)).isTrue();
    }

    @Test
    public void updateUserPassword_ShouldNotPersistUser_WhenGivenInvalidPassword() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user.getUuid(), user.getRole());

        this.assertShouldNotPersistUser_WhenGivenInvalidPassword(password -> {
            this.userService.updateUserPassword(user.getUuid(), password);
        });
    }

    @Test
    public void updateUserPassword_ShouldThrowSecurityException_WhenNotCalledByUser() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.updateUserPassword(UUID.randomUUID(), UNKNOWN_PASSWORD),
                () -> this.loginAs(null, UserRole.STAFF),
                () -> this.logout()
        );
    }

    @Test
    public void updateUserRole_ShouldPersistNewRole_WhenGivenUuidAndPassword() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));

        this.userService.updateUserRole(user.getUuid(), UserRole.STAFF);
        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getRole()).isEqualByComparingTo(UserRole.STAFF);
    }

    @Test
    public void updateUserRole_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.updateUserRole(UUID.randomUUID(), UserRole.STAFF),
                () -> this.loginAs(null, UserRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void removeUser_ShouldRemoveUser_WhenGivenUuid() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user.getUuid(), user.getRole());

        this.userService.removeUser(user.getUuid());
        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void removeUser_ShouldThrowSecurityException_WhenNotCalledByUser() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.removeUser(UUID.randomUUID()),
                () -> this.loginAs(null, UserRole.STAFF),
                () -> this.logout()
        );
    }

    private void assertShouldNotPersistUser_WhenGivenInvalidUsername(CallableOneArgument<String> callable) {
        if (!this.userRepository.findByUsername(ANOTHER_USERNAME).isPresent())
            this.userRepository.save(this.newUserInstance(ANOTHER_USERNAME));

        List<UserDB> expected = this.userRepository.findAll();

        for (String username : Arrays.asList(null, EMPTY_USERNAME, MAX_SIZE_USERNAME, ANOTHER_USERNAME)) {
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> callable.call(username));

            List<UserDB> actual = this.userRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }

    private void assertShouldNotPersistUser_WhenGivenInvalidPassword(CallableOneArgument<String> callable) {
        List<UserDB> expected = this.userRepository.findAll();

        for (String username : Arrays.asList(null, EMPTY_PASSWORD, MAX_SIZE_PASSWORD)) {
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> callable.call(username));

            List<UserDB> actual = this.userRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }

    private UserDB newUserInstance(String username) {
        String encodedPassword = ENCODER.encode(NORMAL_PASSWORD);

        return new UserDB(username, encodedPassword);
    }

    private void loginAs(UUID uuid, UserRole role) {
        UUID newUUID = uuid != null ? uuid : UUID.randomUUID();

        this.securityContext.login(newUUID, role);
    }
}
