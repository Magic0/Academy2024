package com.mitocode.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDTO {

    private String id;

    @NotNull
    @Size(min = 2, max = 50)
    private String nameCourse;

    @NotNull
    @Size(min = 2, max = 10)
    private String acronymsCourse;

    @NotNull
    private Boolean statusCourse;
}
