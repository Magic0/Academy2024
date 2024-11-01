package com.mitocode.config;

import com.mitocode.dto.CourseDTO;
import com.mitocode.dto.EnrollmentDTO;
import com.mitocode.dto.StudentDTO;
import com.mitocode.model.Course;
import com.mitocode.model.Enrollment;
import com.mitocode.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class MapperConfig {

    @Bean(name = "defaultMapper")
    public ModelMapper defaultMapper() {
        return new ModelMapper();
    }

    @Bean(name = "studentMapper")
    public ModelMapper studentMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        //Escritura
        mapper.createTypeMap(StudentDTO.class, Student.class)
                .addMapping(StudentDTO::getName, (dest, v) -> dest.setFirstName((String) v))
                .addMapping(StudentDTO::getSurname, (dest, v) -> dest.setLastName((String) v))
                .addMapping(StudentDTO::getDni, (dest, v) -> dest.setDni((String) v))
                .addMapping(StudentDTO::getAge, (dest, v) -> dest.setAge((v != null) ? (int) v : 0));

        //Lectura
        mapper.createTypeMap(Student.class, StudentDTO.class)
                .addMapping(Student::getFirstName, (dest, v) -> dest.setName((String) v))
                .addMapping(Student::getLastName, (dest, v) -> dest.setSurname((String) v))
                .addMapping(Student::getDni, (dest, v) -> dest.setDni((String) v))
                .addMapping(Student::getAge, (dest, v) -> dest.setAge((Integer) v));

        return mapper;
    }
/*
    @Bean(name = "enrollmentMapper")
    public ModelMapper enrollmentMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.createTypeMap(Course.class, CourseDTO.class);
        mapper.createTypeMap(CourseDTO.class, Course.class);
        mapper.createTypeMap(EnrollmentDTO.class, Enrollment.class)
                .addMapping(e -> e.getStudent().getName(), (dest, v) -> dest.getStudent().setFirstName((String) v))
                .addMapping(e -> e.getStudent().getSurname(), (dest, v) -> dest.getStudent().setLastName((String) v));


        //Lectura
        mapper.createTypeMap(Enrollment.class, EnrollmentDTO.class)
                .addMapping(e -> e.getStudent().getFirstName(), (dest, v) -> dest.getStudent().setName((String) v))
                .addMapping(e -> e.getStudent().getLastName(), (dest, v) -> dest.getStudent().setSurname((String) v));

        return mapper;
    } */

    @Bean(name = "enrollmentMapper")
    public ModelMapper enrollmentMapper() {
        ModelMapper mapper = new ModelMapper();
        ModelMapper mapperCourse = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        Converter<List<CourseDTO>, List<Course>> courseDtoToCourseConverter =
                ctx -> ctx.getSource().stream()
                        .map(courseDto -> mapperCourse.map(courseDto, Course.class))
                        .collect(Collectors.toList());

        Converter<List<Course>, List<CourseDTO>> courseToCourseDtoConverter =
                ctx -> ctx.getSource().stream()
                        .map(course -> mapperCourse.map(course, CourseDTO.class))
                        .collect(Collectors.toList());

        mapper.createTypeMap(EnrollmentDTO.class, Enrollment.class)
                .addMapping(e -> e.getStudent().getName(), (dest, v) -> dest.getStudent().setFirstName((String) v))
                .addMapping(e -> e.getStudent().getSurname(), (dest, v) -> dest.getStudent().setLastName((String) v))
                .addMappings(m -> m.using(courseDtoToCourseConverter).map(EnrollmentDTO::getCourses, Enrollment::setCourses));

        mapper.createTypeMap(Enrollment.class, EnrollmentDTO.class)
                .addMapping(e -> e.getStudent().getFirstName(), (dest, v) -> dest.getStudent().setName((String) v))
                .addMapping(e -> e.getStudent().getLastName(), (dest, v) -> dest.getStudent().setSurname((String) v))
                .addMappings(m -> m.using(courseToCourseDtoConverter).map(Enrollment::getCourses, EnrollmentDTO::setCourses));

        return mapper;
    }
}
