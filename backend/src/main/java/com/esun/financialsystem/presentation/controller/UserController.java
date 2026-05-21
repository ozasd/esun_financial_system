package com.esun.financialsystem.presentation.controller;

import com.esun.financialsystem.business.service.UserService;
import com.esun.financialsystem.presentation.request.GetUserRequest;
import com.esun.financialsystem.presentation.request.PostUserRequest;
import com.esun.financialsystem.presentation.request.PutUserRequest;
import com.esun.financialsystem.presentation.response.DeleteUserResponse;
import com.esun.financialsystem.presentation.response.PagedResponse;
import com.esun.financialsystem.presentation.response.UserMutationResponse;
import com.esun.financialsystem.presentation.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PagedResponse<UserResponse> getUser(@Valid @ModelAttribute GetUserRequest request) {
        return userService.getUser(request);
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserMutationResponse postUser(@Valid @RequestBody PostUserRequest request) {
        String userId = userService.postUser(request);
        return new UserMutationResponse(userId, "User created");
    }

    @PutMapping("/{userId}")
    public UserMutationResponse putUser(
            @PathVariable String userId,
            @Valid @RequestBody PutUserRequest request) {
        String updatedUserId = userService.putUser(userId, request);
        return new UserMutationResponse(updatedUserId, "User updated");
    }

    @DeleteMapping("/{userId}")
    public DeleteUserResponse deleteUser(@PathVariable String userId) {
        return new DeleteUserResponse(userService.deleteUser(userId));
    }
}
