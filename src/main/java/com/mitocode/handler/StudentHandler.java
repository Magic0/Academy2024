package com.mitocode.handler;

import com.mitocode.dto.StudentDTO;
import com.mitocode.model.Student;
import com.mitocode.service.IStudentService;
import com.mitocode.util.SortEnum;
import com.mitocode.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Comparator;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudentHandler {

    private final IStudentService service;

    @Qualifier("studentMapper")
    private final ModelMapper modelMapper;
    private final RequestValidator requestValidator;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll().map(this::convertToDto), StudentDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("id");

        return service.findById(id)
                .map(this::convertToDto)
                .flatMap(e -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(e))
                )
                .switchIfEmpty(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        Mono<StudentDTO> dto = request.bodyToMono(StudentDTO.class);

        return dto.flatMap(requestValidator::validate)
                .flatMap(e -> service.save(convertToDocument(e)))
                .map(this::convertToDto)
                .flatMap(e -> ServerResponse
                        .created(URI.create(request.uri().toString().concat("/").concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(e))
                );
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<StudentDTO> dto = request.bodyToMono(StudentDTO.class);

        return dto.map(e -> {
                    e.setId(id);
                    return e;
                })
                .flatMap(requestValidator::validate)
                .flatMap( e -> service.update(id, convertToDocument(e)))
                .map(this::convertToDto)
                .flatMap( e -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(e))
                ).switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");

        return service.delete(id)
                .flatMap(result ->{
                    if(result){
                        return ServerResponse.ok().build();
                    } else {
                        return ServerResponse.notFound().build();
                    }
                });
    }

    public Mono<ServerResponse> findAllSorted(ServerRequest request) {
        String sort = request.queryParam("sort").orElse("ASC");
        Comparator<Student> comparator = sort.equals(SortEnum.ASC.getValue())
                ? Comparator.comparingInt(Student::getAge)
                : Comparator.comparingInt(Student::getAge).reversed();

        Flux<StudentDTO> fx = service.findAll().sort(comparator).map(this::convertToDto);

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx, StudentDTO.class);
    }

    private StudentDTO convertToDto(Student model){
        return modelMapper.map(model, StudentDTO.class);
    }

    private Student convertToDocument(StudentDTO dto){
        return modelMapper.map(dto, Student.class);
    }
}
