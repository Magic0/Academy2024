package com.mitocode.controller;

import com.mitocode.dto.EnrollmentDTO;
import com.mitocode.model.Enrollment;
import com.mitocode.pagination.PageSupport;
import com.mitocode.service.IEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping("/v0/enrollments")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final IEnrollmentService service;

    @Qualifier("enrollmentMapper")
    private final ModelMapper mapper;

    @Qualifier("defaultMapper")
    private final ModelMapper mapperCourse;

    @GetMapping
    public Mono<ResponseEntity<Flux<EnrollmentDTO>>> findAll() {
        Flux<EnrollmentDTO> fx = service.findAll().map(this::convertToDto);

        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<EnrollmentDTO>> findById(@PathVariable("id") String id) {
        return service.findById(id)
                .map(this::convertToDto)
                .map(e-> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<EnrollmentDTO>> save(@Valid @RequestBody EnrollmentDTO enrollment, final ServerHttpRequest req) {
        return service.save(convertToDocument(enrollment))
                .map(this::convertToDto)
                .map(e-> ResponseEntity.created(
                        URI.create(req.getURI().toString().concat("/").concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<EnrollmentDTO>> update(@PathVariable("id") String id, @Valid @RequestBody EnrollmentDTO enrollment) {
        return Mono.just(enrollment)
                .map(e -> {
                    e.setId(id);
                    return e;
                }).flatMap(e -> service.update(id,convertToDocument(e)))
                .map(this::convertToDto)
                .map(e -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("id") String id) {
        return service.delete(id)
                .flatMap(result -> {
                    if(result){
                        return Mono.just(ResponseEntity.noContent().build());
                    }else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<EnrollmentDTO>> getHateoas(@PathVariable("id") String id){
        Mono<Link> monoLink = linkTo(methodOn(EnrollmentController.class).findById(id)).withRel("dish-link").toMono();

        return service.findById(id)
                .map(this::convertToDto)
                .zipWith(monoLink, EntityModel::of);
    }

    @GetMapping("/pageable")
    public Mono<ResponseEntity<PageSupport<EnrollmentDTO>>> getPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "2") int size
    ){
        return service.getPage(PageRequest.of(page, size))
                .map(pageSupport -> new PageSupport<>(
                        pageSupport.getContent().stream().map(this::convertToDto).toList(),
                        pageSupport.getPageNumber(),
                        pageSupport.getPageSize(),
                        pageSupport.getTotalElements()
                ))
                .map(e -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/generateReport/{id}")
    public Mono<ResponseEntity<byte[]>> generateReport(@PathVariable("id") String id){
        return service.generateReport(id)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(bytes)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private EnrollmentDTO convertToDto(Enrollment model){

        return mapper.map(model, EnrollmentDTO.class);
    }

    private Enrollment convertToDocument(EnrollmentDTO dto){
        return mapper.map(dto, Enrollment.class);
    }
/*
    private Enrollment convertToDocument(EnrollmentDTO dto){
        Enrollment enrollment = mapper.map(dto, Enrollment.class);
        List<Course> mappedCourses = dto.getCourses().stream()
                .map(courseDTO -> mapperCourse.map(courseDTO, Course.class))
                .collect(Collectors.toList());
        System.out.println(mappedCourses);
        enrollment.setCourses(mappedCourses);
        return enrollment;
    }*/
}
