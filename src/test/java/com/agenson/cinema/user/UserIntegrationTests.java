package com.agenson.cinema.user;

import com.agenson.cinema.order.OrderDB;
import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.security.SecurityService;
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

import javax.persistence.EntityManager;
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
    private ModelMapper mapper;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private UserDB defaultUser;

    @BeforeEach
    public void setup() {
        this.defaultUser = this.userRepository.save(this.newUserInstance(UNKNOWN_USERNAME));

        this.loginAs(SecurityRole.STAFF);
    }

    @AfterEach
    public void logout() {
        this.securityService.logout();
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
        assertThat(this.userService.findUser(ANOTHER_USERNAME).isPresent()).isFalse();
    }

    @Test
    public void findUsers_ShouldReturnUserList() {
        List<UserDB> userList = Arrays.asList(
                this.newUserInstance(NORMAL_USERNAME),
                this.newUserInstance(ANOTHER_USERNAME),
                this.defaultUser
        );

        assertThat(this.userRepository.findAll().size()).isEqualTo(1);

        this.userRepository.saveAll(userList);

        List<UserDetailsDTO> actual = this.userService.findUsers();
        List<UserDetailsDTO> expected = userList.stream()
                .map(user -> new UserDetailsDTO(user.getUuid(), user.getUsername(), user.getRole()))
                .collect(Collectors.toList());

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
        UserDTO expected = this.userService.createUser(NORMAL_USERNAME, NORMAL_PASSWORD);

        Optional<UserDB> actualDB = this.userRepository.findByUuid(expected.getUuid());
        Optional<UserDTO> actualDTO = actualDB.map(user -> this.mapper.map(user, UserDTO.class));

        assertThat(actualDTO.isPresent()).isTrue();
        assertThat(actualDTO.get()).isEqualTo(expected);

        String encodedPassword = actualDB.get().getPassword();
        assertThat(this.encoder.matches(NORMAL_PASSWORD, encodedPassword)).isTrue();
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
        this.loginAs(user);

        Optional<UserDTO> expected = this.userService.updateUserUsername(user.getUuid(), ANOTHER_USERNAME);
        Optional<UserDTO> actual = this.userRepository.findByUuid(user.getUuid())
                .map(userDB -> this.mapper.map(userDB, UserDTO.class));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateUserUsername_ShouldNotPersistUser_WhenGivenInvalidUsername() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user);

        this.assertShouldNotPersistUser_WhenGivenInvalidUsername(username -> {
            this.userService.updateUserUsername(user.getUuid(), username);
        });
    }

    @Test
    public void updateUserUsername_ShouldThrowSecurityException_WhenNotCalledByUser() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.updateUserUsername(UUID.randomUUID(), UNKNOWN_USERNAME),
                () -> this.loginAs(SecurityRole.STAFF),
                () -> this.logout()
        );
    }

    @Test
    public void updateUserPassword_ShouldPersistNewPassword_WhenGivenUuidAndPassword() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user);

        this.userService.updateUserPassword(user.getUuid(), ANOTHER_PASSWORD);
        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isTrue();

        String encodedPassword = actual.get().getPassword();
        assertThat(this.encoder.matches(ANOTHER_PASSWORD, encodedPassword)).isTrue();
    }

    @Test
    public void updateUserPassword_ShouldNotPersistUser_WhenGivenInvalidPassword() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        this.loginAs(user);

        this.assertShouldNotPersistUser_WhenGivenInvalidPassword(password -> {
            this.userService.updateUserPassword(user.getUuid(), password);
        });
    }

    @Test
    public void updateUserPassword_ShouldThrowSecurityException_WhenNotCalledByUser() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.updateUserPassword(UUID.randomUUID(), UNKNOWN_PASSWORD),
                () -> this.loginAs(SecurityRole.STAFF),
                () -> this.logout()
        );
    }

    @Test
    public void updateUserRole_ShouldPersistNewRole_WhenGivenUuidAndPassword() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));

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

    @Test
    public void removeUser_ShouldRemoveUserAndOrder_WhenGivenUuid() {
        UserDB user = this.userRepository.save(this.newUserInstance(NORMAL_USERNAME));
        OrderDB order = new OrderDB(user);

        this.entityManager.persist(order);
        this.entityManager.refresh(user);

        assertThat(this.entityManager.contains(order)).isTrue();

        this.loginAs(user);
        this.userService.removeUser(user.getUuid());

        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isFalse();
        assertThat(this.entityManager.contains(order)).isFalse();
    }

    @Test
    public void removeUser_ShouldThrowSecurityException_WhenNotCalledByUser() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.userService.removeUser(UUID.randomUUID()),
                () -> this.loginAs(SecurityRole.STAFF),
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
        String encodedPassword = this.encoder.encode(NORMAL_PASSWORD);

        return new UserDB(username, encodedPassword);
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
