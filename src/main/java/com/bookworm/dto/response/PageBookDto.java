package com.bookworm.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageBookDto {
    private List<BookSummaryDto> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    public List<BookSummaryDto> getContent() { return content; }
    public void setContent(List<BookSummaryDto> content) { this.content = content; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
}
