package com.agenson.cinema.user;

import com.agenson.cinema.utils.CallableOneArgument;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class UserIntegrationTests implements UserConstants {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void findUser_ShouldReturnPersistedUser_WhenGivenUuidOrUsername() {
        UserDB user = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD));

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
                new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD),
                new UserDB(ANOTHER_USERNAME, NORMAL_PASSWORD)
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
    public void loginUser_ShouldReturnPersistedUser_WhenGivenCredentials() {
        UserDB user = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD));

        UserDTO expected = this.mapper.map(user, UserDTO.class);
        UserDTO actual = this.userService.loginUser(user.getUsername(), user.getPassword());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void loginUser_ShouldThrowInvalidUserException_WhenGivenInvalidCredentials() {
        UserDB user = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD));

        assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> this.userService.loginUser(UNKNOWN_USERNAME, user.getPassword()));

        assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> this.userService.loginUser(user.getUsername(), UNKNOWN_PASSWORD));
    }

    @Test
    public void createUser_ShouldReturnPersistedUser_WhenGivenCredentials() {
        UserDTO expected = this.userService.createUser(NORMAL_USERNAME, NORMAL_PASSWORD);
        Optional<UserDTO> actual = this.userRepository.findByUuid(expected.getUuid())
                .map(user -> this.mapper.map(user, UserDTO.class));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
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
        UUID uuid = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD)).getUuid();

        Optional<UserDTO> expected = this.userService.updateUserUsername(uuid, ANOTHER_USERNAME);
        Optional<UserDTO> actual = this.userRepository.findByUuid(uuid)
                .map(user -> this.mapper.map(user, UserDTO.class));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateUserUsername_ShouldNotPersistUser_WhenGivenInvalidUsername() {
        UUID uuid = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD)).getUuid();

        this.assertShouldNotPersistUser_WhenGivenInvalidUsername(username -> {
            this.userService.updateUserUsername(uuid, username);
        });
    }

    @Test
    public void updateUserPassword_ShouldPersistNewPassword_WhenGivenUuidAndPassword() {
        UserDB user = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD));

        this.userService.updateUserPassword(user.getUuid(), ANOTHER_PASSWORD);

        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getPassword()).isEqualTo(ANOTHER_PASSWORD);
    }

    @Test
    public void updateUserPassword_ShouldNotPersistUser_WhenGivenInvalidPassword() {
        UUID uuid = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD)).getUuid();

        this.assertShouldNotPersistUser_WhenGivenInvalidPassword(password -> {
            this.userService.updateUserPassword(uuid, password);
        });
    }

    @Test
    public void updateUserRole_ShouldPersistNewRole_WhenGivenUuidAndPassword() {
        UserDB user = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD));

        this.userService.updateUserRole(user.getUuid(), Role.STAFF);

        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getRole()).isEqualByComparingTo(Role.STAFF);
    }

    @Test
    public void removeUser_ShouldRemoveUser_WhenGivenUuid() {
        UserDB user = this.userRepository.save(new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD));

        this.userService.removeUser(user.getUuid());
        Optional<UserDB> actual = this.userRepository.findByUuid(user.getUuid());

        assertThat(actual.isPresent()).isFalse();
    }

    private void assertShouldNotPersistUser_WhenGivenInvalidUsername(CallableOneArgument<String> callable) {
        if (!this.userRepository.findByUsername(ANOTHER_USERNAME).isPresent())
            this.userRepository.save(new UserDB(ANOTHER_USERNAME, ANOTHER_PASSWORD));

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
}
