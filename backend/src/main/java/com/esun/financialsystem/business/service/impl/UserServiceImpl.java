package com.esun.financialsystem.business.service.impl;

import com.esun.financialsystem.business.service.UserService;
import com.esun.financialsystem.common.exception.BadRequestException;
import com.esun.financialsystem.common.exception.ResourceNotFoundException;
import com.esun.financialsystem.data.repository.UserRepository;
import com.esun.financialsystem.presentation.request.GetUserRequest;
import com.esun.financialsystem.presentation.request.PostUserRequest;
import com.esun.financialsystem.presentation.request.PutUserRequest;
import com.esun.financialsystem.presentation.response.PagedResponse;
import com.esun.financialsystem.presentation.response.UserResponse;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_SORT_COLUMNS =
            Set.of("user_id", "user_name", "email", "account", "created_at", "updated_at");

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PagedResponse<UserResponse> getUser(GetUserRequest request) {
        validatePage(request.getPage());
        validatePageSize(request.getPageSize());
        validateSortBy(request.getSortBy());
        validateSortDirection(request.getSortDirection());

        if (request.getUserId() != null && request.getUserId().isBlank()) {
            throw new BadRequestException("userId must not be blank");
        }

        return new PagedResponse<>(
                userRepository.getUser(request),
                userRepository.countUser(request),
                request.getPage() == null ? 1 : request.getPage(),
                request.getPageSize() == null ? 10 : request.getPageSize());
    }

    @Override
    public UserResponse getUserById(String userId) {
        validateUserId(userId);
        return userRepository.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User %s does not exist".formatted(userId)));
    }

    @Override
    public String postUser(PostUserRequest request) {
        validateUserId(request.userId());
        return userRepository.postUser(
                request.userId().trim(),
                request.userName().trim(),
                request.email().trim(),
                request.account().trim());
    }

    @Override
    public String putUser(String userId, PutUserRequest request) {
        validateUserId(userId);
        return userRepository.putUser(
                        userId.trim(),
                        request.userName().trim(),
                        request.email().trim(),
                        request.account().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User %s does not exist".formatted(userId)));
    }

    @Override
    public boolean deleteUser(String userId) {
        validateUserId(userId);
        if (!userRepository.deleteUser(userId.trim())) {
            throw new ResourceNotFoundException("User %s does not exist".formatted(userId));
        }
        return true;
    }

    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BadRequestException("userId must not be blank");
        }
    }

    private void validatePage(Integer page) {
        if (page != null && page < 1) {
            throw new BadRequestException("page must be greater than 0");
        }
    }

    private void validatePageSize(Integer pageSize) {
        if (pageSize != null && (pageSize < 1 || pageSize > 100)) {
            throw new BadRequestException("pageSize must be between 1 and 100");
        }
    }

    private void validateSortBy(String sortBy) {
        if (StringUtils.hasText(sortBy) && !ALLOWED_SORT_COLUMNS.contains(sortBy.trim())) {
            throw new BadRequestException("sortBy is not supported");
        }
    }

    private void validateSortDirection(String sortDirection) {
        if (!StringUtils.hasText(sortDirection)) {
            return;
        }
        String normalized = sortDirection.trim().toUpperCase();
        if (!"ASC".equals(normalized) && !"DESC".equals(normalized)) {
            throw new BadRequestException("sortDirection must be ASC or DESC");
        }
    }
}
