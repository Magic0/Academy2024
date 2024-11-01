package com.mitocode.config;

import com.mitocode.handler.CourseHandler;
import com.mitocode.handler.EnrollmentHandler;
import com.mitocode.handler.StudentHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
public class RouterConfig {

    private static final String coursesPATH = "v1/courses";
    private static final String studentsPATH = "v1/students";
    private static final String enrollmentsPATH = "v1/enrollments";
    //Functional Endpoints
    @Bean
    public RouterFunction<ServerResponse> routesCourse(CourseHandler handler) { 
        return RouterFunctions.route()
                .GET(coursesPATH, handler::findAll)
                .GET(coursesPATH + "/{id}", handler::findById)
                .POST(coursesPATH, handler::save)
                .PUT(coursesPATH + "/{id}", handler::update)
                .DELETE(coursesPATH + "/{id}", handler::delete)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> routesStudent(StudentHandler handler) { 
        return RouterFunctions.route()
                .GET(studentsPATH, handler::findAll)
                .GET(studentsPATH + "/sorted", handler::findAllSorted) //si lo pongo debajo del id agarra el sorted como id
                .GET(studentsPATH + "/{id}", handler::findById)
                .POST(studentsPATH, handler::save)
                .PUT(studentsPATH + "/{id}", handler::update)
                .DELETE(studentsPATH + "/{id}", handler::delete)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> routesEnrollment(EnrollmentHandler handler) { 
        return RouterFunctions.route()
                .GET(enrollmentsPATH, handler::findAll)
                .GET(enrollmentsPATH + "/generateReport/{id}", handler::generateReport)
                .GET(enrollmentsPATH + "/{id}", handler::findById)
                .POST(enrollmentsPATH, handler::save)
                .PUT(enrollmentsPATH + "/{id}", handler::update)
                .DELETE(enrollmentsPATH + "/{id}", handler::delete)
                .build();
    }
}
