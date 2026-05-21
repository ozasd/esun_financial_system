package com.esun.financialsystem.business.service;

import com.esun.financialsystem.presentation.request.GetUserRequest;
import com.esun.financialsystem.presentation.request.PostUserRequest;
import com.esun.financialsystem.presentation.request.PutUserRequest;
import com.esun.financialsystem.presentation.response.PagedResponse;
import com.esun.financialsystem.presentation.response.UserResponse;

public interface UserService {

    PagedResponse<UserResponse> getUser(GetUserRequest request);

    UserResponse getUserById(String userId);

    String postUser(PostUserRequest request);

    String putUser(String userId, PutUserRequest request);

    boolean deleteUser(String userId);
}
