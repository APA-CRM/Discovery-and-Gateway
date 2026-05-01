package com.crm.gateway.composite.utils;

import com.crm.gateway.composite.dtos.request.TaskFilterRequest;
import com.crm.sharedlib.core.dto.DateRange;
import com.crm.sharedlib.core.dto.request.BaseFilterRequest;
import com.crm.sharedlib.core.enums.SortDirections;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FilterRequestToMapConvertorTest {

    @Test
    public void testConvert_whenSimpleObject_thenSuccess() {
        BaseFilterRequest request = new TaskFilterRequest();

        request.setPage(0);
        request.setSize(10);
        request.setSortBy("id");
        request.setSortDirection(SortDirections.ASC);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        try {
            map = FilterRequestToMapConvertor.convert(request);
        } catch (Throwable e) {
            fail("Error has occurred while converting filter request to map", e);
        }

        assertFalse(map.isEmpty(), "Map is empty");

        assertEquals(request.getPage().toString(), map.get("page").getFirst());
        assertEquals(request.getSize().toString(), map.get("size").getFirst());
        assertEquals(request.getSortBy(), map.get("sortBy").getFirst());
        assertEquals(request.getSortDirection().toString(), map.get("sortDirection").getFirst());
    }

    @Test
    public void testConvert_whenComplexObject_thenSuccess() {
        TaskFilterRequest request = new TaskFilterRequest();

        request.setPage(0);
        request.setSize(10);
        request.setSortBy("id");
        request.setSortDirection(SortDirections.ASC);

        request.setCreatedAt(new DateRange(Instant.now(), Instant.now()));
        request.setTitle("title");
        request.setAssignedTo(1L);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        try {
            map = FilterRequestToMapConvertor.convert(request);
        } catch (Throwable e) {
            fail("Error has occurred while converting filter request to map", e);
        }

        assertFalse(map.isEmpty());

        assertEquals(request.getPage().toString(), map.get("page").getFirst());
        assertEquals(request.getSize().toString(), map.get("size").getFirst());
        assertEquals(request.getSortBy(), map.get("sortBy").getFirst());
        assertEquals(request.getSortDirection().toString(), map.get("sortDirection").getFirst());
        assertEquals(request.getTitle(), map.get("title").getFirst());
        assertEquals(request.getCreatedAt().getFrom().toString(), map.get("createdAt.from").getFirst());
        assertEquals(request.getCreatedAt().getTo().toString(), map.get("createdAt.to").getFirst());
        assertEquals(request.getAssignedTo().toString(), map.get("assignedTo").getFirst());
    }

}