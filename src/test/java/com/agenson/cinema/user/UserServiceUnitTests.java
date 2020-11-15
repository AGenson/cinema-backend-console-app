package com.agenson.cinema.user;

import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.utils.CallableOneArgument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests implements UserConstants {

    private static final HashMap<String, InvalidUserException.Type> INVALID_USER_USERNAMES =
            new HashMap<String, InvalidUserException.Type>() {{
                put(null, InvalidUserException.Type.USERNAME_MANDATORY);
                put(EMPTY_USERNAME, InvalidUserException.Type.USERNAME_MANDATORY);
                put(MAX_SIZE_USERNAME, InvalidUserException.Type.USERNAME_MAXSIZE);
                put(ANOTHER_USERNAME, InvalidUserException.Type.USERNAME_EXISTS);
            }};

    private static final HashMap<String, InvalidUserException.Type> INVALID_USER_PASSWORDS =
            new HashMap<String, InvalidUserException.Type>() {{
                put(null, InvalidUserException.Type.PASSWORD_MANDATORY);
                put(EMPTY_PASSWORD, InvalidUserException.Type.PASSWORD_MANDATORY);
                put(MAX_SIZE_PASSWORD, InvalidUserException.Type.PASSWORD_MAXSIZE);
            }};

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void findUser_ShouldReturnUser_WhenGivenUuid() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

        UserCompleteDTO expected = new UserCompleteDTO(user);
        Optional<UserCompleteDTO> actual = this.userService.findUser(user.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findUser_ShouldReturnNull_WhenGivenUnknownUuid() {
        when(this.userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());

        assertThat(this.userService.findUser(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.userService.findUser((UUID) null).isPresent()).isFalse();
    }

    @Test
    public void findUsers_ShouldReturnUserList() {
        List<UserDB> userList = Arrays.asList(
                this.newUserInstance(NORMAL_USERNAME),
                this.newUserInstance(ANOTHER_USERNAME)
        );

        when(this.userRepository.findAll()).thenReturn(userList);

        List<UserDetailsDTO> actual = this.userService.findUsers();
        List<UserDetailsDTO> expected = userList.stream().map(UserDetailsDTO::new).collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createUser_ShouldReturnUser_WhenGivenCredentials() {
        when(this.userRepository.findByUsername(NORMAL_USERNAME)).thenReturn(Optional.empty());
        when(this.userRepository.save(any(UserDB.class))).then(returnsFirstArg());
        when(this.encoder.encode(NORMAL_PASSWORD)).thenReturn(ENCODER.encode(NORMAL_PASSWORD));

        UserBasicDTO actual = this.userService.createUser(NORMAL_USERNAME, NORMAL_PASSWORD);

        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual.getUsername()).isEqualTo(NORMAL_USERNAME);
    }

    @Test
    public void createUser_ShouldThrowAssociatedInvalidUserException_WhenGivenInvalidCredentials() {
        this.assertShouldThrowInvalidUserException_WhenGivenInvalidUsername(username -> {
            this.userService.createUser(username, NORMAL_PASSWORD);
        });

        when(this.userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        this.assertShouldThrowInvalidUserException_WhenGivenInvalidPassword(password -> {
            this.userService.createUser(NORMAL_USERNAME, password);
        });
    }

    @Test
    public void updateUserUsername_ShouldReturnModifiedUser_WhenGivenUuidAndUsername() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.save(any(UserDB.class))).then(returnsFirstArg());
        when(this.userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(this.userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Optional<UserBasicDTO> actual = this.userService.updateUserUsername(user.getUuid(), ANOTHER_USERNAME);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getUsername()).isEqualTo(ANOTHER_USERNAME);
    }

    @Test
    public void updateUserUsername_ShouldThrowAssociatedInvalidMovieException_WhenGivenInvalidUsername() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.of(user));

        this.assertShouldThrowInvalidUserException_WhenGivenInvalidUsername(username -> {
            this.userService.updateUserUsername(user.getUuid(), username);
        });
    }

    @Test
    public void updateUserPassword_ShouldReturnUser_WhenGivenUuidAndPassword() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.save(any(UserDB.class))).then(returnsFirstArg());
        when(this.userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(this.encoder.encode(ANOTHER_PASSWORD)).thenReturn(ENCODER.encode(ANOTHER_PASSWORD));

        UserBasicDTO expected = new UserBasicDTO(user);
        Optional<UserBasicDTO> actual = this.userService.updateUserPassword(user.getUuid(), ANOTHER_PASSWORD);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void updateUserPassword_ShouldThrowAssociatedInvalidUserException_WhenGivenInvalidPassword() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.of(user));

        this.assertShouldThrowInvalidUserException_WhenGivenInvalidPassword(password -> {
            this.userService.updateUserPassword(user.getUuid(), password);
        });
    }

    @Test
    public void updateUserRole_ShouldReturnUser_WhenGivenUuidAndRole() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.save(any(UserDB.class))).then(returnsFirstArg());
        when(this.userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

        UserBasicDTO expected = new UserBasicDTO(user);
        Optional<UserBasicDTO> actual = this.userService.updateUserRole(user.getUuid(), SecurityRole.STAFF);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    private void assertShouldThrowInvalidUserException_WhenGivenInvalidUsername(CallableOneArgument<String> callable) {
        when(this.userRepository.findByUsername(anyString())).thenAnswer(invocation -> {
            UserDB userWithSameUsername = this.newUserInstance(invocation.getArgument(0));

            return Optional.of(userWithSameUsername);
        });

        for (Map.Entry<String, InvalidUserException.Type> pair : INVALID_USER_USERNAMES.entrySet())
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> callable.call(pair.getKey()))
                    .withMessage(pair.getValue().toString());
    }

    private void assertShouldThrowInvalidUserException_WhenGivenInvalidPassword(CallableOneArgument<String> callable) {
        for (Map.Entry<String, InvalidUserException.Type> pair : INVALID_USER_PASSWORDS.entrySet())
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> callable.call(pair.getKey()))
                    .withMessage(pair.getValue().toString());
    }

    private UserDB newUserInstance(String username) {
        String encodedPassword = ENCODER.encode(NORMAL_PASSWORD);

        return new UserDB(username, encodedPassword);
    }
}
