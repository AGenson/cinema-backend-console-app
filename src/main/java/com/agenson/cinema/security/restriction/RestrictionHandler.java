package com.agenson.cinema.security.restriction;

import com.agenson.cinema.security.SecurityException;
import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.lang.Integer.min;

@Aspect
@Component
@RequiredArgsConstructor
public class RestrictionHandler {

    private final SecurityService securityService;

    @Before("@annotation(RestrictToStaff)")
    public void restrictToStaff() throws SecurityException {
        if (!this.securityService.isLoggedIn())
            throw new SecurityException(SecurityException.Type.IDENTIFICATION);
        else if (!this.securityService.hasRole(SecurityRole.STAFF))
            throw new SecurityException(SecurityException.Type.AUTHORIZATION);
    }

    @Before("@annotation(RestrictToUser)")
    public void restrictToUser(JoinPoint joinPoint) throws SecurityException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RestrictToUser annotation = signature.getMethod().getAnnotation(RestrictToUser.class);
        List<String> argNames = Arrays.asList(signature.getParameterNames());
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        Optional<UUID> uuid = Optional.empty();

        for (int i = 0; i < min(args.size(), argNames.size()); i++) {
            if (Objects.equals(argNames.get(i), annotation.argName()) && args.get(i) instanceof UUID) {
                uuid = Optional.of((UUID) args.get(i));
                break;
            }
        }

        if (!this.securityService.isLoggedIn())
            throw new SecurityException(SecurityException.Type.IDENTIFICATION);
        else if (!uuid.isPresent() || !this.securityService.isUser(uuid.get()))
            throw new SecurityException(SecurityException.Type.AUTHORIZATION);
    }
}
