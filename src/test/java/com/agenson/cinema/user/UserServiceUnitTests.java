package com.agenson.cinema.user;

import com.agenson.cinema.utils.CallableOneArgument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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
    private ModelMapper mapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setup() {
        lenient().when(this.mapper.map(any(UserDB.class), ArgumentMatchers.<Class<UserDTO>>any()))
                .thenAnswer(invocation -> {
                    UserDB user = invocation.getArgument(0);

                    return new UserDTO(user.getUuid(), user.getUsername());
                });
    }

    @Test
    public void findUser_ShouldReturnUser_WhenGivenUuidOrUsername() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(this.userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDTO expected = this.mapper.map(user, UserDTO.class);
        Optional<UserDTO> actual = this.userService.findUser(user.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);

        actual = this.userService.findUser(user.getUsername());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findUser_ShouldReturnNull_WhenGivenUnknownUuidOrUsername() {
        when(this.userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());
        when(this.userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThat(this.userService.findUser(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.userService.findUser((UUID) null).isPresent()).isFalse();

        assertThat(this.userService.findUser(UNKNOWN_USERNAME).isPresent()).isFalse();
        assertThat(this.userService.findUser((String) null).isPresent()).isFalse();
    }

    @Test
    public void findUsers_ShouldReturnUserList() {
        List<UserDB> userList = Arrays.asList(
                this.newUserInstance(NORMAL_USERNAME),
                this.newUserInstance(ANOTHER_USERNAME)
        );

        when(this.userRepository.findAll()).thenReturn(userList);

        List<UserDTO> actual = this.userService.findUsers();
        List<UserDTO> expected = userList.stream()
                .map(user -> this.mapper.map(user, UserDTO.class))
                .collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void loginUser_ShouldReturnUser_WhenGivenCredentials() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(this.passwordEncoder.matches(NORMAL_PASSWORD, user.getPassword())).thenReturn(true);

        UserDTO expected = this.mapper.map(user, UserDTO.class);
        UserDTO actual = this.userService.loginUser(user.getUsername(), NORMAL_PASSWORD);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void loginUser_ShouldThrowInvalidUserException_WhenGivenInvalidCredentials() {
        UserDB user = this.newUserInstance(NORMAL_USERNAME);

        when(this.userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(this.userRepository.findByUsername(UNKNOWN_USERNAME)).thenReturn(Optional.empty());
        when(this.userRepository.findByUsername(null)).thenReturn(Optional.empty());

        when(this.passwordEncoder.matches(UNKNOWN_PASSWORD, user.getPassword())).thenReturn(false);
        when(this.passwordEncoder.matches(null, user.getPassword())).thenReturn(false);

        for (String username : Arrays.asList(null, UNKNOWN_USERNAME))
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> this.userService.loginUser(username, user.getPassword()))
                    .withMessage(InvalidUserException.Type.CONNECTION.toString());

        for (String password : Arrays.asList(null, UNKNOWN_PASSWORD))
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> this.userService.loginUser(user.getUsername(), password))
                    .withMessage(InvalidUserException.Type.CONNECTION.toString());
    }

    @Test
    public void createUser_ShouldReturnUser_WhenGivenCredentials() {
        when(this.userRepository.findByUsername(NORMAL_USERNAME)).thenReturn(Optional.empty());
        when(this.userRepository.save(any(UserDB.class))).then(returnsFirstArg());
        when(this.passwordEncoder.encode(NORMAL_PASSWORD)).thenReturn(ENCODER.encode(NORMAL_PASSWORD));

        UserDTO actual = this.userService.createUser(NORMAL_USERNAME, NORMAL_PASSWORD);

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

        UserDTO expected = this.mapper.map(user, UserDTO.class);
        expected.setUsername(ANOTHER_USERNAME);

        Optional<UserDTO> actual = this.userService.updateUserUsername(user.getUuid(), ANOTHER_USERNAME);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
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
        when(this.passwordEncoder.encode(ANOTHER_PASSWORD)).thenReturn(ENCODER.encode(ANOTHER_PASSWORD));

        UserDTO expected = this.mapper.map(user, UserDTO.class);
        Optional<UserDTO> actual = this.userService.updateUserPassword(user.getUuid(), ANOTHER_PASSWORD);

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

        UserDTO expected = this.mapper.map(user, UserDTO.class);
        Optional<UserDTO> actual = this.userService.updateUserRole(user.getUuid(), Role.STAFF);

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
