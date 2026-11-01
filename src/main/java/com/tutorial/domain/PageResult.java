package com.tutorial.domain;

import java.util.List;

/** Framework-free page of results with paging metadata. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
