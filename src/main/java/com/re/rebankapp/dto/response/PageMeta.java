package com.re.rebankapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageMeta {
    private int currentPage;
    private int pageSize;
    private int totalPage;
    private long totalElements;
}
