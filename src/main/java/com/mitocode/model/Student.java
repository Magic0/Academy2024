package com.mitocode.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "students")
public class Student {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Field
    private String firstName;

    @Field
    private String lastName;

    @Field
    private String dni;

    @Field
    private int age;
}
