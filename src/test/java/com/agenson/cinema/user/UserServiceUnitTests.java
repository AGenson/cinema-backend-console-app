package com.agenson.cinema.user;

import com.agenson.cinema.security.SecurityRole;
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

    private static final String NORMAL_PASSWORD_ENCODED = new BCryptPasswordEncoder().encode(NORMAL_PASSWORD);

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

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void findUser_ShouldReturnUser_WhenGivenUuid() {
        UserDB user = new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD_ENCODED);

        when(this.userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

        UserCompleteDTO expected = new UserCompleteDTO(user);
        Optional<UserCompleteDTO> actual = this.userService.findUser(user.getUuid());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    // Mock test only: will throw SecurityException when not logged in as user
    public void findUser_ShouldReturnNull_WhenGivenUnknownUuid() {
        when(this.userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());

        assertThat(this.userService.findUser(UUID.randomUUID())).isEmpty();
        assertThat(this.userService.findUser(null)).isEmpty();
    }

    @Test
    public void findUsers_ShouldReturnUserList() {
        List<UserDB> userList = Arrays.asList(
                new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD_ENCODED),
                new UserDB(ANOTHER_USERNAME, NORMAL_PASSWORD_ENCODED)
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
        when(this.encoder.encode(NORMAL_PASSWORD)).thenReturn(NORMAL_PASSWORD_ENCODED);

        UserBasicDTO actual = this.userService.createUser(NORMAL_USERNAME, NORMAL_PASSWORD);

        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual.getUsername()).isEqualTo(NORMAL_USERNAME);
    }

    @Test
    public void createUser_ShouldThrowAssociatedInvalidUserException_WhenGivenInvalidCredentials() {
        when(this.userRepository.findByUsername(anyString())).thenAnswer(invocation -> {
            UserDB userWithSameUsername = new UserDB(invocation.getArgument(0), NORMAL_PASSWORD_ENCODED);

            return Optional.of(userWithSameUsername);
        });

        for (Map.Entry<String, InvalidUserException.Type> pair : INVALID_USER_USERNAMES.entrySet())
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> this.userService.createUser(pair.getKey(), NORMAL_PASSWORD))
                    .withMessage(pair.getValue().toString());

        when(this.userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        for (Map.Entry<String, InvalidUserException.Type> pair : INVALID_USER_PASSWORDS.entrySet())
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> this.userService.createUser(NORMAL_USERNAME, pair.getKey()))
                    .withMessage(pair.getValue().toString());
    }

    @Test
    public void updateUserRole_ShouldReturnUser_WhenGivenUuidAndRole() {
        UserDB user = new UserDB(NORMAL_USERNAME, NORMAL_PASSWORD_ENCODED);

        when(this.userRepository.save(any(UserDB.class))).then(returnsFirstArg());
        when(this.userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

        Optional<UserDetailsDTO> actual = this.userService.updateUserRole(user.getUuid(), SecurityRole.STAFF);

        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getRole()).isEqualTo(SecurityRole.STAFF);
    }
}
