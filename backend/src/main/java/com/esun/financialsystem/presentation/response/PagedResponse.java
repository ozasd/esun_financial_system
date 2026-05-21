package com.esun.financialsystem.presentation.response;

import java.util.List;

public record PagedResponse<T>(
        List<T> datas,
        long total,
        int page,
        int pageSize) {
}
