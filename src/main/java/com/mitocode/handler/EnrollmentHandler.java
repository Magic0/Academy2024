package com.mitocode.handler;

import com.mitocode.dto.EnrollmentDTO;
import com.mitocode.model.Enrollment;
import com.mitocode.service.IEnrollmentService;
import com.mitocode.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentHandler {

    private final IEnrollmentService service;

    @Qualifier("enrollmentMapper")
    private final ModelMapper modelMapper;
    private final RequestValidator requestValidator;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll().map(this::convertToDto), EnrollmentDTO.class);
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
        Mono<EnrollmentDTO> dto = request.bodyToMono(EnrollmentDTO.class);

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
        Mono<EnrollmentDTO> dto = request.bodyToMono(EnrollmentDTO.class);

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

    public Mono<ServerResponse> generateReport(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.generateReport(id)
                .flatMap(bytes -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(fromValue(bytes))
               ).switchIfEmpty(ServerResponse.notFound().build());
    }

    private EnrollmentDTO convertToDto(Enrollment model){
        return modelMapper.map(model, EnrollmentDTO.class);
    }

    private Enrollment convertToDocument(EnrollmentDTO dto){

        return modelMapper.map(dto, Enrollment.class);
    }
}
