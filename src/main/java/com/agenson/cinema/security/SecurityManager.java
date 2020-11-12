package com.agenson.cinema.security;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.*;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityManager {

    private final SecurityContext securityContext;

    @Before("@annotation(RestrictToStaff)")
    public void restrictToStaff() throws SecurityException {
        if (!this.securityContext.isLoggedIn())
            throw new SecurityException(SecurityException.Type.IDENTIFICATION);
        else if (!this.securityContext.hasRole(UserRole.STAFF))
            throw new SecurityException(SecurityException.Type.AUTHORIZATION);
    }

    @Before("@annotation(RestrictToUser)")
    public void restrictToUser(JoinPoint joinPoint) throws SecurityException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        List<String> argNames = Arrays.asList(signature.getParameterNames());
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        Optional<UUID> uuid = Optional.empty();

        for (int i = 0; i < args.size(); i++) {
            if (Objects.equals(argNames.get(i), "uuid") && args.get(i) instanceof UUID) {
                uuid = Optional.of((UUID) args.get(i));
                break;
            }
        }

        if (!this.securityContext.isLoggedIn())
            throw new SecurityException(SecurityException.Type.IDENTIFICATION);
        else if (uuid.isPresent() && !this.securityContext.isUser(uuid.get()))
            throw new SecurityException(SecurityException.Type.AUTHORIZATION);
    }
}
