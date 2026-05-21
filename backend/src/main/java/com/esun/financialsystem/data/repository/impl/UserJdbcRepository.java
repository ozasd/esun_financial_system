package com.esun.financialsystem.data.repository.impl;

import com.esun.financialsystem.data.mapper.UserRowMapper;
import com.esun.financialsystem.data.repository.UserRepository;
import com.esun.financialsystem.data.sql.UserSql;
import com.esun.financialsystem.presentation.request.GetUserRequest;
import com.esun.financialsystem.presentation.response.UserResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        List<Object> parameters = new ArrayList<>();
        String sql = UserSql.SELECT_PREFIX
                + buildWhereClause(request, parameters)
                + buildOrderClause(request)
                + " LIMIT ? OFFSET ?";
        parameters.add(resolvePageSize(request));
        parameters.add(resolveOffset(request));
        return jdbcTemplate.query(sql, userRowMapper, parameters.toArray());
    }

    @Override
    public long countUser(GetUserRequest request) {
        List<Object> parameters = new ArrayList<>();
        Long total = jdbcTemplate.queryForObject(
                UserSql.COUNT_PREFIX + buildWhereClause(request, parameters),
                Long.class,
                parameters.toArray());
        return total == null ? 0L : total;
    }

    @Override
    public Optional<UserResponse> getUserById(String userId) {
        List<UserResponse> users = jdbcTemplate.query(UserSql.SELECT_BY_ID, userRowMapper, userId);
        return users.stream().findFirst();
    }

    @Override
    public String postUser(String userId, String userName, String email, String account) {
        return jdbcTemplate.queryForObject(
                UserSql.INSERT,
                String.class,
                userId,
                userName,
                email,
                account);
    }

    @Override
    public Optional<String> putUser(String userId, String userName, String email, String account) {
        List<String> userIds = jdbcTemplate.query(
                UserSql.UPDATE,
                (rs, rowNum) -> rs.getString("user_id"),
                userName,
                email,
                account,
                userId);
        return userIds.stream().findFirst();
    }

    @Override
    public boolean deleteUser(String userId) {
        List<String> userIds = jdbcTemplate.query(
                UserSql.DELETE,
                (rs, rowNum) -> rs.getString("user_id"),
                userId);
        return !userIds.isEmpty();
    }

    private String buildWhereClause(GetUserRequest request, List<Object> parameters) {
        StringBuilder whereClause = new StringBuilder(" WHERE TRUE");
        String userId = trimToNull(request.getUserId());
        String userName = trimToNull(request.getUserName());
        String email = trimToNull(request.getEmail());
        String account = trimToNull(request.getAccount());
        String keyword = toKeywordPattern(request.getKeyword());

        if (userId != null) {
            whereClause.append(" AND u.user_id = ?");
            parameters.add(userId);
        }
        if (userName != null) {
            whereClause.append(" AND u.user_name ILIKE ?");
            parameters.add("%" + userName + "%");
        }
        if (email != null) {
            whereClause.append(" AND u.email ILIKE ?");
            parameters.add("%" + email + "%");
        }
        if (account != null) {
            whereClause.append(" AND u.account = ?");
            parameters.add(account);
        }
        if (keyword != null) {
            whereClause.append("""
                     AND (
                        u.user_id ILIKE ?
                        OR u.user_name ILIKE ?
                        OR u.email ILIKE ?
                        OR u.account ILIKE ?
                    )
                    """);
            for (int i = 0; i < 4; i++) {
                parameters.add(keyword);
            }
        }
        return whereClause.toString();
    }

    private String buildOrderClause(GetUserRequest request) {
        Map<String, String> allowedColumns = new LinkedHashMap<>();
        allowedColumns.put("user_id", "u.user_id");
        allowedColumns.put("user_name", "u.user_name");
        allowedColumns.put("email", "u.email");
        allowedColumns.put("account", "u.account");
        allowedColumns.put("created_at", "u.created_at");
        allowedColumns.put("updated_at", "u.updated_at");

        String orderColumn = allowedColumns.getOrDefault(trimToNull(request.getSortBy()), "u.user_id");
        String direction = "DESC".equalsIgnoreCase(trimToNull(request.getSortDirection())) ? "DESC" : "ASC";
        return " ORDER BY " + orderColumn + " " + direction;
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

    private String toKeywordPattern(String keyword) {
        return StringUtils.hasText(keyword) ? "%" + keyword.trim() + "%" : null;
    }
}
