package com.esun.financialsystem.data.repository.impl;

import com.esun.financialsystem.data.mapper.UserRowMapper;
import com.esun.financialsystem.data.repository.UserRepository;
import com.esun.financialsystem.data.sql.UserSql;
import com.esun.financialsystem.presentation.request.GetUserRequest;
import com.esun.financialsystem.presentation.response.UserResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class UserJdbcRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public List<UserResponse> getUser(GetUserRequest request) {
        return jdbcTemplate.query(
                UserSql.GET_USERS,
                userRowMapper,
                trimToNull(request.getUserId()),
                trimToNull(request.getUserName()),
                trimToNull(request.getEmail()),
                trimToNull(request.getAccount()),
                trimToNull(request.getKeyword()),
                resolveSortBy(request),
                resolveSortDirection(request),
                resolvePageSize(request),
                resolveOffset(request));
    }

    @Override
    public long countUser(GetUserRequest request) {
        Long total = jdbcTemplate.queryForObject(
                UserSql.COUNT_USERS,
                Long.class,
                trimToNull(request.getUserId()),
                trimToNull(request.getUserName()),
                trimToNull(request.getEmail()),
                trimToNull(request.getAccount()),
                trimToNull(request.getKeyword()));
        return total == null ? 0L : total;
    }

    @Override
    public Optional<UserResponse> getUserById(String userId) {
        List<UserResponse> users = jdbcTemplate.query(UserSql.GET_USER_BY_ID, userRowMapper, userId);
        return users.stream().findFirst();
    }

    @Override
    public String postUser(String userId, String userName, String email, String account) {
        return jdbcTemplate.queryForObject(
                UserSql.ADD_USER,
                String.class,
                userId,
                userName,
                email,
                account);
    }

    @Override
    public Optional<String> putUser(String userId, String userName, String email, String account) {
        String updatedUserId = jdbcTemplate.queryForObject(
                UserSql.UPDATE_USER,
                String.class,
                userId,
                userName,
                email,
                account);
        return Optional.ofNullable(updatedUserId);
    }

    @Override
    public boolean deleteUser(String userId) {
        String deletedUserId = jdbcTemplate.queryForObject(
                UserSql.DELETE_USER,
                String.class,
                userId);
        return deletedUserId != null;
    }

    private int resolveOffset(GetUserRequest request) {
        return (resolvePage(request) - 1) * resolvePageSize(request);
    }

    private int resolvePage(GetUserRequest request) {
        return request.getPage() == null ? 1 : request.getPage();
    }

    private int resolvePageSize(GetUserRequest request) {
        return request.getPageSize() == null ? 10 : request.getPageSize();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String resolveSortBy(GetUserRequest request) {
        String sortBy = trimToNull(request.getSortBy());
        return sortBy == null ? "user_id" : sortBy;
    }

    private String resolveSortDirection(GetUserRequest request) {
        String sortDirection = trimToNull(request.getSortDirection());
        return "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
    }
}
