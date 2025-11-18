package com.hashjosh.application.dto.update;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUpdateDto {
    private String fullName;
    private String coordinates;
    private Map<String, Object> dynamicFields = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getDynamicFields() {
        return dynamicFields;
    }

    @JsonAnySetter
    public void setDynamicField(String key, Object value) {
        this.dynamicFields.put(key, value);
    }
}
