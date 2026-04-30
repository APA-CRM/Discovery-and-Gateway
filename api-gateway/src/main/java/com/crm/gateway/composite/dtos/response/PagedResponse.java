package com.crm.gateway.composite.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private PageMetadata page;

    public PagedResponse(List<T> content, PagedModel.PageMetadata metadata) {
        this.content = content;
        this.page = new PageMetadata(metadata);
    }

}
