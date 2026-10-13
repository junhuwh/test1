package com.bookworm.dto.response;

import lombok.*;

import java.net.URI;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailResponse {
    private URI type = URI.create("about:blank");
    private String title;
    private Integer status;
    private String detail;
    private URI instance;

    public ProblemDetailResponse(URI type, String title, Integer status, String detail, URI instance) {
        this.type = type != null ? type : URI.create("about:blank");
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public static ProblemDetailResponseBuilder builder() {
        return new ProblemDetailResponseBuilder();
    }

    public static class ProblemDetailResponseBuilder {
        private URI type = URI.create("about:blank");
        private String title;
        private Integer status;
        private String detail;
        private URI instance;

        public ProblemDetailResponseBuilder type(URI type) {
            this.type = type;
            return this;
        }

        public ProblemDetailResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ProblemDetailResponseBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public ProblemDetailResponseBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public ProblemDetailResponseBuilder instance(URI instance) {
            this.instance = instance;
            return this;
        }

        public ProblemDetailResponse build() {
            return new ProblemDetailResponse(type, title, status, detail, instance);
        }
    }
}
