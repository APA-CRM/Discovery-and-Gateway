package com.crm.gateway.composite.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.web.PagedModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageMetadata {

    private Long size;
    private Long number;
    private Long totalElements;
    private Long totalPages;

    public PageMetadata(PagedModel.PageMetadata metadata) {
        this.size = metadata.size();
        this.number = metadata.number();
        this.totalElements = metadata.totalElements();
        this.totalPages = metadata.totalPages();
    }

}
