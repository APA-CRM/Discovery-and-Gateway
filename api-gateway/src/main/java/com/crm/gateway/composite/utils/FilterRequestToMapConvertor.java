package com.crm.gateway.composite.utils;

import com.crm.sharedlib.core.dto.request.BaseFilterRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@UtilityClass
public class FilterRequestToMapConvertor {

    public static MultiValueMap<String, String> convert(final BaseFilterRequest request) {
        Assert.notNull(request, "Request is null");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        addBaseFields(request, map);

        try {
            addFieldsToMap(map, request, null, request.getClass().getDeclaredFields());
        } catch (Exception e) {
            log.error("Error has occurred while mapping BaseFilterRequest to MultiValueMap", e);
            throw new RuntimeException(e);
        }

        return map;
    }

    private static void addBaseFields(final BaseFilterRequest request, MultiValueMap<String, String> map) {
        map.put("page", Collections.singletonList(request.getPage().toString()));
        map.put("size", Collections.singletonList(request.getSize().toString()));
        map.put("sortBy", Collections.singletonList(request.getSortBy()));
        map.put("sortDirection", Collections.singletonList(request.getSortDirection().toString()));
    }

    private static void addFieldsToMap(
            MultiValueMap<String, String> map, Object object,
            String root, Field[] fields
    ) throws IllegalAccessException {

        for (Field field : fields) {
            field.setAccessible(true);

            String fieldName = field.getName();

            String currentFieldName = root == null
                    ? fieldName : root + "." + fieldName;

            Object fieldObject = field.get(object);

            if (fieldObject == null) {
                continue;
            }

            if (fieldObject instanceof Collection<?> collection) {
                ArrayList<String> values = new ArrayList<>(collection.size());

                collection.forEach(object1 -> values.add(object1.toString()));

                map.put(currentFieldName, values);
            } else if (isSimpleObject(fieldObject)) {
                map.put(currentFieldName, Collections.singletonList(fieldObject.toString()));
            } else {
                addFieldsToMap(map, fieldObject, currentFieldName, fieldObject.getClass().getDeclaredFields());
            }
        }

    }

    private static boolean isSimpleObject(Object object) {
        return switch (object) {
            case Integer ignored -> true;
            case Long ignored -> true;
            case String ignored -> true;
            case Instant ignored -> true;
            case Enum<?> ignored -> true;
            default -> false;
        };
    }

}
