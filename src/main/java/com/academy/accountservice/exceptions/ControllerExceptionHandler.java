package com.academy.accountservice.exceptions;

import com.academy.accountservice.logging.SecurityEvent;
import com.academy.accountservice.logging.SecurityEventLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private final SecurityEventLogger Logger = SecurityEventLogger.getInstance();

    private final Map<String, String> badRequestMessages = Map.of("UserExistsException", "User exist!",
            "PasswordBreachedException", "The password is in the hacker's database!",
            "PasswordNotDifferentException", "The passwords must be different!",
            "WrongEmployeePaymentException", "Employee-payment pair is not unique!",
            "AdministratorDeleteException", "Can't remove ADMINISTRATOR role!",
            "RoleAbsentException", "The user does not have a role!",
            "NoRolesException", "The user must have at least one role!",
            "RoleCombineViolationException", "The user cannot combine administrative and business roles!",
            "AdministratorLockException", "Can't lock the ADMINISTRATOR!");

    private final Map<String, String> notFoundMessages = Map.of(
            "UserNotFoundException", "User was not found!",
            "RoleNotFoundException", "Role not found!",
            "OperationNotFoundException", "Incorrect operation!");

    @ExceptionHandler({UserExistsException.class,
            PasswordBreachedException.class,
            PasswordNotDifferentException.class,
            WrongEmployeePaymentException.class,
            AdministratorDeleteException.class,
            RoleAbsentException.class,
            NoRolesException.class,
            RoleCombineViolationException.class,
            AdministratorLockException.class})
    public ResponseEntity<Object> handleCustomBadRequests(
            CustomException e, WebRequest request) {

        String exceptionName = e.getExceptionName();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        CustomErrorMessage body = new CustomErrorMessage(status, badRequestMessages.get(exceptionName),
                request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler({UserNotFoundException.class,
            RoleNotFoundException.class,
            OperationNotFoundException.class})
    public ResponseEntity<Object> handleCustomNotFound(
            CustomException e, WebRequest request) {

        String exceptionName = e.getExceptionName();
        HttpStatus status = HttpStatus.NOT_FOUND;

        CustomErrorMessage body = new CustomErrorMessage(status, notFoundMessages.get(exceptionName),
                request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAuthenticationException(Exception ex, WebRequest request) {

        HttpStatus status = HttpStatus.FORBIDDEN;
        String path = request.getDescription(false).substring(4);

        CustomErrorMessage body = new CustomErrorMessage(status, "Access Denied!", path);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Logger.log(SecurityEvent.ACCESS_DENIED, userDetails.getUsername(), path, path);

        return new ResponseEntity<>(body, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        CustomErrorMessage body = new CustomErrorMessage(status,
                "Password length must be 12 chars minimum!",
                request.getDescription(false).substring(4));
        /*
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            ((FieldError) error).
            if (fieldName.equals("password")) {
                body.put("message", ((FieldError) error).getDefaultMessage());
            }
        });

        if (!body.containsKey("message")) {
            body.put("message", "Validation is not passed!");
        }

        body.put("path", request.getDescription(false)); */

        return new ResponseEntity<>(body, headers, status);
    }
}
