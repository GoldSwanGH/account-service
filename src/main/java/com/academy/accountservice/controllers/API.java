package com.academy.accountservice.controllers;

import com.academy.accountservice.data.*;
import com.academy.accountservice.data.entities.*;
import com.academy.accountservice.exceptions.*;
import com.academy.accountservice.logging.SecurityEvent;
import com.academy.accountservice.logging.SecurityEventLogger;
import com.academy.accountservice.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Validated
public class API {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeePaymentRepository paymentRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlatformTransactionManager trManager;

    private final SecurityEventLogger Logger = SecurityEventLogger.getInstance();

    static final List<String> breachedPasswords = List.of("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly",
            "PasswordForAugust", "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember",
            "PasswordForDecember");

    @PostMapping("/auth/signup")
    public Map<String, Object> signUp(HttpServletRequest request, @Valid @RequestBody UserSignUpModel body) {

        if (breachedPasswords.contains(body.getPassword())) {
            throw new PasswordBreachedException();
        }

        if (!userRepository.findByUsername(body.getEmail().toLowerCase(Locale.ROOT)).isPresent()) {

            User user = new User();
            user.setUsername(body.getEmail().toLowerCase(Locale.ROOT));
            user.setLastname(body.getLastname());
            user.setName(body.getName());
            user.setPassword(passwordEncoder.encode(body.getPassword()));

            if (userRepository.count() == 0) {
                Role role = new Role();
                role.setRole(RoleName.ROLE_ADMINISTRATOR);
                user.addRole(role);
            }

            Role role = new Role();
            role.setRole(RoleName.ROLE_USER);
            role.setUser(user);
            user.addRole(role);
            user = userRepository.save(user);
            List<String> roles = new ArrayList<>();

            user.getRoles().forEach(r -> {
                roles.add(r.getRole().name());
            });

            Logger.log(SecurityEvent.CREATE_USER, "Anonymous", user.getUsername(), request.getRequestURI());

            return Map.of("id", user.getId(),
                    "name", user.getName(),
                    "lastname", user.getLastname(),
                    "email", user.getUsername(),
                    "roles", roles.stream().sorted().collect(Collectors.toList()));
        }

        throw new UserExistsException();
    }

    @PostMapping("/auth/changepass")
    public Map<String, Object> changePass(HttpServletRequest request, @Valid @RequestBody ChangePassModel body,
                                          @AuthenticationPrincipal UserDetails details) {

        if (breachedPasswords.contains(body.getNewPassword())) {
            throw new PasswordBreachedException();
        }

        User user = userRepository.findByUsername(details.getUsername().toLowerCase(Locale.ROOT)).orElseGet(() -> {
            throw new UserNotFoundException();
        });

        if (passwordEncoder.matches(body.getNewPassword(), user.getPassword())) {
            throw new PasswordNotDifferentException();
        }

        user.setPassword(passwordEncoder.encode(body.getNewPassword()));

        userRepository.save(user);

        Logger.log(SecurityEvent.CHANGE_PASSWORD, user.getUsername(), user.getUsername(), request.getRequestURI());

        return Map.of("email", details.getUsername(),
                "status", "The password has been updated successfully");
    }

    @PostMapping("/acct/payments")
    public Map<String, Object> postPayments(@RequestBody
                                            @NotEmpty(message = "Input payments list cannot be empty!")
                                            List<@Valid EmployeePaymentModel> body) {

        TransactionDefinition trDefinition = new DefaultTransactionDefinition();
        TransactionStatus trStatus = trManager.getTransaction(trDefinition);

        for (EmployeePaymentModel entry : body) {
            userRepository.findByUsername(entry.getEmployee().toLowerCase(Locale.ROOT)).ifPresentOrElse(user -> {
                try {
                    EmployeePayment payment = new EmployeePayment();
                    payment.setPeriod(entry.getPeriod());
                    payment.setSalary(entry.getSalary());
                    payment.setEmployee(user);
                    paymentRepository.save(payment);
                } catch (Exception ex) {
                    trManager.rollback(trStatus);
                    throw new WrongEmployeePaymentException();
                }
            }, () -> {
                throw new UserNotFoundException();
            });
        }

        return Map.of("status", "Added successfully!");
    }

    @PutMapping("/acct/payments")
    public Map<String, Object> putPayment(@Valid @RequestBody EmployeePaymentModel body) {

        TransactionDefinition trDefinition = new DefaultTransactionDefinition();
        TransactionStatus trStatus = trManager.getTransaction(trDefinition);

        userRepository.findByUsername(body.getEmployee().toLowerCase(Locale.ROOT)).ifPresentOrElse(user -> {
            try {
                EmployeePayment payment = new EmployeePayment();
                payment.setPeriod(body.getPeriod());
                payment.setSalary(body.getSalary());
                payment.setEmployee(user);
                paymentRepository.save(payment);
            } catch (Exception ex) {
                trManager.rollback(trStatus);
                throw new WrongEmployeePaymentException();
            }
        }, () -> {
            throw new UserNotFoundException();
        });

        return Map.of("status", "Updated successfully!");
    }

    @GetMapping("/empl/payment")
    public Object payment(@RequestParam @Null
                          @Pattern(regexp = "(0[1-9]|1[0-2])-(19\\d{2}|20\\d{2})$")
                          String period, @AuthenticationPrincipal UserDetails details) {

        User user = userRepository.findByUsername(details.getUsername().toLowerCase(Locale.ROOT)).orElseGet(() -> {
            throw new UserNotFoundException();
        });

        if (period == null) {

            List<Map<String, Object>> responseList = new ArrayList<>();
            List<EmployeePayment> payments = user.getPayments();
            payments.sort(Comparator.comparing(EmployeePayment::getPeriod));

            for (EmployeePayment elem : payments) {
                Map<String, Object> responseMap = new LinkedHashMap<>();
                responseMap.put("name", user.getName());
                responseMap.put("lastname", user.getLastname());
                responseMap.put("period", elem.getPeriod());
                responseMap.put("salary", String.format("%d dollar(s) %d cent(s)",
                        elem.getSalary() / 100, elem.getSalary() % 100));
                responseList.add(responseMap);
            }
            return responseList;
        } else {

            EmployeePayment payment = user.getPayments().stream()
                    .filter(p -> p.getPeriod().equals(period)).findFirst()
                    .orElseThrow(WrongEmployeePaymentException::new);

            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("name", user.getName());
            responseMap.put("lastname", user.getLastname());
            responseMap.put("period", payment.getPeriod());
            responseMap.put("salary", String.format("%d dollar(s) %d cent(s)",
                    payment.getSalary() / 100, payment.getSalary() % 100));
            return responseMap;
        }
    }

    @GetMapping("/admin/user")
    public List<Map<String, Object>> getUsers() {

        List<User> result = userRepository.findAll();
        result.sort(Comparator.comparing(User::getId));
        List<Map<String, Object>> response = new ArrayList<>();

        result.forEach(user -> {
            List<String> roles = new ArrayList<>();
            user.getRoles().forEach(r -> {
                roles.add(r.getRole().name());
            });
            response.add(Map.of("id", user.getId(),
                    "name", user.getName(),
                    "lastname", user.getLastname(),
                    "email", user.getUsername(),
                    "roles", roles.stream().sorted().collect(Collectors.toList())));
        });

        return response;
    }

    @DeleteMapping("/admin/user")
    public Map<String, Object> deleteUser(HttpServletRequest request, @RequestParam String email,
                                          @AuthenticationPrincipal UserDetails details) {

        User user = userRepository.findByUsername(email.toLowerCase(Locale.ROOT))
                .orElseThrow(UserNotFoundException::new);

        user.getRoles().forEach(role -> {
            if (role.getRole().equals(RoleName.ROLE_ADMINISTRATOR)) {
                throw new AdministratorDeleteException();
            }
        });

        userRepository.delete(user);

        Logger.log(SecurityEvent.DELETE_USER, details.getUsername(), user.getUsername(), request.getRequestURI());

        return Map.of("user", user.getUsername(),
                "status", "Deleted successfully!");
    }

    @PutMapping("/admin/user/role")
    public Map<String, Object> putRole(HttpServletRequest request, @Valid @RequestBody PutRoleModel body,
                                       @AuthenticationPrincipal UserDetails details) {

        RoleOperation roleOperation;
        try {
            roleOperation = RoleOperation.valueOf(body.getOperation());
        } catch (IllegalArgumentException ex) {
            throw new OperationNotFoundException();
        }

        RoleName roleName;
        try {
            roleName = RoleName.valueOf(body.getRole());
        } catch (IllegalArgumentException ex) {
            throw new RoleNotFoundException();
        }

        if (roleName.equals(RoleName.ROLE_ADMINISTRATOR) && roleOperation.equals(RoleOperation.REMOVE)) {
            throw new AdministratorDeleteException();
        }

        User user = userRepository.findByUsername(body.getUser().toLowerCase(Locale.ROOT))
                .orElseThrow(UserNotFoundException::new);

        if (roleOperation.equals(RoleOperation.REMOVE) && user.getRoles().size() <= 1) {
            throw new NoRolesException();
        }

        boolean roleFoundFlag = false;
        for (Role role : user.getRoles()) {
            if (role.getRole().equals(RoleName.ROLE_ADMINISTRATOR) && roleName.equals(RoleName.ROLE_ACCOUNTANT) ||
                    role.getRole().equals(RoleName.ROLE_ACCOUNTANT) && roleName.equals(RoleName.ROLE_ADMINISTRATOR)) {
                throw new RoleCombineViolationException();
            }
            if (role.getRole().equals(roleName)) {
                roleFoundFlag = true;

                if (roleOperation.equals(RoleOperation.REMOVE)) {
                    Set<Role> roles = user.getRoles();
                    roles.remove(role);
                    user.setRoles(roles);

                    userRepository.save(user);

                    Logger.log(SecurityEvent.REMOVE_ROLE, details.getUsername(),
                            "Remove role " + roleName.name() + " from " + user.getUsername(),
                            request.getRequestURI());
                }

                break;
            }
        }
        if (!roleFoundFlag && roleOperation.equals(RoleOperation.REMOVE)) {
            throw new RoleAbsentException();
        }

        if (!roleFoundFlag && roleOperation.equals(RoleOperation.GRANT)) {
            Role role = new Role();
            role.setUser(user);
            role.setRole(roleName);
            user.addRole(role);

            userRepository.save(user);

            Logger.log(SecurityEvent.GRANT_ROLE, details.getUsername(),
                    "Grant role " + roleName.name() + " to " + user.getUsername(),
                    request.getRequestURI());
        }

        List<String> roles = new ArrayList<>();
        user.getRoles().forEach(r -> {
            roles.add(r.getRole().name());
        });
        return Map.of("id", user.getId(),
                "name", user.getName(),
                "lastname", user.getLastname(),
                "email", user.getUsername(),
                "roles", roles.stream().sorted().collect(Collectors.toList()));
    }

    @PutMapping("/admin/user/access")
    public Map<String, Object> putAccess(HttpServletRequest request, @Valid @RequestBody PutAccessModel body,
                                         @AuthenticationPrincipal UserDetails details) {

        UserOperation userOperation;
        try {
            userOperation = UserOperation.valueOf(body.getOperation());
        } catch (IllegalArgumentException ex) {
            throw new OperationNotFoundException();
        }

        User user = userRepository.findByUsername(body.getUser().toLowerCase(Locale.ROOT))
                .orElseThrow(UserNotFoundException::new);

        if (userOperation.equals(UserOperation.LOCK)) {
            user.getRoles().forEach(role -> {
                if (role.getRole().equals(RoleName.ROLE_ADMINISTRATOR)) {
                    throw new AdministratorLockException();
                }
            });
            user.setAccountNonLocked(false);

            userRepository.save(user);

            Logger.log(SecurityEvent.LOCK_USER, details.getUsername(),
                    "Lock user " + user.getUsername(),
                    request.getRequestURI());
        } else {
            user.setAccountNonLocked(true);

            userRepository.save(user);

            Logger.log(SecurityEvent.UNLOCK_USER, details.getUsername(),
                    "Unlock user " + user.getUsername(),
                    request.getRequestURI());
        }

        return Map.of("status", String.format("User %s %s!", user.getUsername(), userOperation.name().toLowerCase()));
    }

    @GetMapping("/security/events")
    public List<Log> getSecurityEvents() {

        return logRepository.findAllByOrderByIdAsc();
    }
}
