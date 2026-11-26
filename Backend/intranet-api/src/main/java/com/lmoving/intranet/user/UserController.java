package com.lmoving.intranet.user;

import com.lmoving.intranet.user.dto.CreateUserRequest;
import com.lmoving.intranet.user.dto.UpdateUserRequest;
import com.lmoving.intranet.user.dto.UserResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USERS.VIEW')")
    public List<UserResponse> getUsers() {  
        return userService.getAllUsers();
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAuthority('USERS.VIEW')")
    public UserResponse getUser(
            @PathVariable String username
    ) {
        return userService.getUser(username);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USERS.CREATE')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {

        UserResponse response =
                userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasAuthority('USERS.EDIT')")
    public UserResponse updateUser(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        return userService.updateUser(
                username,
                request
        );
    }

    @PatchMapping("/{username}/active")
    @PreAuthorize("hasAuthority('USERS.DELETE')")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @RequestParam boolean active
    ) {

        userService.setActive(username, active);

        return ResponseEntity.noContent().build();
    }
}