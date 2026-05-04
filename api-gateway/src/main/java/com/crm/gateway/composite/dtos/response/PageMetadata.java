package com.crm.gateway.composite.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageMetadata {

    private Long size;
    private Long number;
    private Long totalElements;
    private Long totalPages;

}
