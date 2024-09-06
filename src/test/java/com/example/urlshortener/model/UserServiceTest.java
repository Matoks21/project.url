package com.example.urlshortener.model;

import com.example.urlshortener.repository.RoleRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        bindingResult = mock(BindingResult.class);
        roleRepository=mock(RoleRepository.class);
        userService = new UserService(userRepository, passwordEncoder,roleRepository);

    }


    @Test
    void testRegisterUser_EmailAlreadyInUse() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("ValidPassword1");
        user.setUsername("user");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        userService.registerUser(user, bindingResult);

        verify(bindingResult).addError(any(FieldError.class));
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void testRegisterUser_UsernameAlreadyInUse() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("ValidPassword1");
        user.setEmail("test@example.com");
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        userService.registerUser(user, bindingResult);

        verify(bindingResult).addError(any(FieldError.class));
        verify(userRepository, times(0)).save(any(User.class));

    }


    @Test
    void testRegisterUser_SuccessfulRegistration() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("ValidPassword1");
        user.setEmail("test@example.com");

        Role role = new Role();
        role.setName(ERole.ROLE_USER); // Використовуємо ERole

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(role)); // Використовуємо ERole

        userService.registerUser(user, bindingResult);


        verify(bindingResult, never()).addError(any(FieldError.class));

        verify(userRepository, times(1)).save(argThat(u ->
                u.getUsername().equals("user") &&
                        u.getPassword().equals("encodedPassword") &&
                        u.getEmail().equals("test@example.com")
        ));
    }



    @Test
    void testLoadUserByUsername_UserFound() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));

        User foundUser = userService.loadUserByUsername("test@example.com");

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(java.util.Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent@example.com");
        });

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }
    @Test
    void testRegisterUser_PasswordInvalid() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("short"); // Невірний пароль
        user.setEmail("test@example.com");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);

        userService.registerUser(user, bindingResult);

        verify(bindingResult).addError(any(FieldError.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testIsValidPassword() {

        assertTrue(userService.isValidPassword("Valid1234"));

        assertFalse(userService.isValidPassword("short"));
        assertFalse(userService.isValidPassword("NoDigitsUppercase"));
        assertFalse(userService.isValidPassword("12345678"));
        assertFalse(userService.isValidPassword("UPPERCASE123"));
    }
}
