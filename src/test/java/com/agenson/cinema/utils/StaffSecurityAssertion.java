package com.agenson.cinema.utils;

import com.agenson.cinema.security.SecurityException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class StaffSecurityAssertion {

    public static void assertShouldThrowSecurityException(
            Callable callable,
            Callable login,
            Callable logout
    ) {
        logout.call();

        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(callable::call)
                .withMessage(SecurityException.Type.IDENTIFICATION.toString());

        login.call();

        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(callable::call)
                .withMessage(SecurityException.Type.AUTHORIZATION.toString());
    }
}
