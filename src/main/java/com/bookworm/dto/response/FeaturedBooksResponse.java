package com.bookworm.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturedBooksResponse {
    private List<BookSummaryDto> recommended;
    private List<BookSummaryDto> bestsellers;
    private List<BookSummaryDto> newLaunches;

    public List<BookSummaryDto> getRecommended() { return recommended; }
    public void setRecommended(List<BookSummaryDto> recommended) { this.recommended = recommended; }
    public List<BookSummaryDto> getBestsellers() { return bestsellers; }
    public void setBestsellers(List<BookSummaryDto> bestsellers) { this.bestsellers = bestsellers; }
    public List<BookSummaryDto> getNewLaunches() { return newLaunches; }
    public void setNewLaunches(List<BookSummaryDto> newLaunches) { this.newLaunches = newLaunches; }
}
