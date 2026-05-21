package com.esun.financialsystem.data.repository;

import com.esun.financialsystem.presentation.request.GetUserRequest;
import com.esun.financialsystem.presentation.response.UserResponse;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<UserResponse> getUser(GetUserRequest request);

    long countUser(GetUserRequest request);

    Optional<UserResponse> getUserById(String userId);

    String postUser(String userId, String userName, String email, String account);

    Optional<String> putUser(String userId, String userName, String email, String account);

    boolean deleteUser(String userId);
}
