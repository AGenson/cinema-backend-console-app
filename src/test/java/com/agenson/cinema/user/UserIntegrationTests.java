package com.agenson.cinema.user;

import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.utils.StaffSecurityAssertion;
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

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private UserDB defaultUser;

    private String defaultPasswordEncoded;

    @BeforeEach
    public void setup() {
        this.defaultPasswordEncoded = this.encoder.encode(NORMAL_PASSWORD);
        this.defaultUser = this.userRepository.save(new UserDB(UNKNOWN_USERNAME, this.defaultPasswordEncoded));

        this.loginAs(SecurityRole.STAFF);
    }

    @AfterEach
    public void logout() {
        this.securityService.logout();
    }

    @Test
    public void findUser_ShouldReturnPersistedUser_WhenGivenUuid() {
        UserDB user = this.userRepository.save(new UserDB(NORMAL_USERNAME, this.defaultPasswordEncoded));
        this.loginAs(user);

        UserCompleteDTO expected = new UserCompleteDTO(user);
        Optional<UserCompleteDTO> actual = this.userService.findUser(user.getUuid());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findUser_ShouldThrowSecurityException_WhenNotLoggedInAsUser() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.findUser(UUID.randomUUID()),
                () -> this.loginAs(SecurityRole.STAFF),
                () -> this.logout()
        );
    }

    @Test
    public void findUsers_ShouldReturnUserList() {
        List<UserDB> userList = Arrays.asList(
                new UserDB(NORMAL_USERNAME, this.defaultPasswordEncoded),
                new UserDB(ANOTHER_USERNAME, this.defaultPasswordEncoded),
                this.defaultUser
        );

        this.userRepository.saveAll(userList);

        assertThat(this.userRepository.findAll().size()).isEqualTo(userList.size());

        List<UserDetailsDTO> actual = this.userService.findUsers();
        List<UserDetailsDTO> expected = userList.stream().map(UserDetailsDTO::new).collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findUsers_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.findUsers(),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void createUser_ShouldReturnPersistedUser_WhenGivenCredentials() {
        UserBasicDTO expected = this.userService.createUser(NORMAL_USERNAME, NORMAL_PASSWORD);

        Optional<UserDB> actualDB = this.userRepository.findByUuid(expected.getUuid());
        Optional<UserBasicDTO> actualDTO = actualDB.map(UserBasicDTO::new);

        assertThat(actualDTO).isNotEmpty();
        assertThat(actualDTO.get()).isEqualTo(expected);

        String encodedPassword = actualDB.get().getPassword();
        assertThat(this.encoder.matches(NORMAL_PASSWORD, encodedPassword)).isTrue();
    }

    @Test
    public void createUser_ShouldNotPersistUser_WhenGivenInvalidUsername() {
        if (!this.userRepository.findByUsername(ANOTHER_USERNAME).isPresent())
            this.userRepository.save(new UserDB(ANOTHER_USERNAME, this.defaultPasswordEncoded));

        List<UserDB> expected = this.userRepository.findAll();

        for (String username : Arrays.asList(null, EMPTY_USERNAME, MAX_SIZE_USERNAME, ANOTHER_USERNAME)) {
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> this.userService.createUser(username, NORMAL_PASSWORD));

            List<UserDB> actual = this.userRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }

    @Test
    public void createUser_ShouldNotPersistUser_WhenGivenInvalidPassword() {
        List<UserDB> expected = this.userRepository.findAll();

        for (String password : Arrays.asList(null, EMPTY_PASSWORD, MAX_SIZE_PASSWORD)) {
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> this.userService.createUser(NORMAL_USERNAME, password));

            List<UserDB> actual = this.userRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }

    @Test
    public void updateUserRole_ShouldPersistNewRole_WhenGivenUuidAndPassword() {
        UserDB user = this.userRepository.save(new UserDB(NORMAL_USERNAME, this.defaultPasswordEncoded));

        this.userService.updateUserRole(user.getUuid(), SecurityRole.STAFF);
        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getRole()).isEqualByComparingTo(SecurityRole.STAFF);
    }

    @Test
    public void updateUserRole_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.updateUserRole(UUID.randomUUID(), SecurityRole.STAFF),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    private void loginAs(SecurityRole role) {
        this.defaultUser.setRole(role);
        this.userRepository.save(this.defaultUser);
        this.loginAs(this.defaultUser);
    }

    private void loginAs(UserDB user) {
        this.securityService.login(user.getUsername(), NORMAL_PASSWORD);
    }
}
