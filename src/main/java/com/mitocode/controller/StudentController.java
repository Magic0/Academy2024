package com.mitocode.controller;

import com.mitocode.dto.StudentDTO;
import com.mitocode.model.Student;
import com.mitocode.pagination.PageSupport;
import com.mitocode.service.IStudentService;
import com.mitocode.util.SortEnum;
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
import java.util.Comparator;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping("/v0/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final IStudentService service;

    @Qualifier("studentMapper")
    private final ModelMapper mapper;

    @GetMapping
    public Mono<ResponseEntity<Flux<StudentDTO>>> findAll() {
        Flux<StudentDTO> fx = service.findAll().map(this::convertToDto);

        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<StudentDTO>> findById(@PathVariable("id") String id) {
        return service.findById(id)
                .map(this::convertToDto)
                .map(e-> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<StudentDTO>> save(@Valid @RequestBody StudentDTO student, final ServerHttpRequest req) {
        return service.save(convertToDocument(student))
                .map(this::convertToDto)
                .map(e-> ResponseEntity.created(
                        URI.create(req.getURI().toString().concat("/").concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<StudentDTO>> update(@PathVariable("id") String id, @Valid @RequestBody StudentDTO student) {
        return Mono.just(student)
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
    public Mono<EntityModel<StudentDTO>> getHateoas(@PathVariable("id") String id){
        Mono<Link> monoLink = linkTo(methodOn(StudentController.class).findById(id)).withRel("dish-link").toMono();

        return service.findById(id)
                .map(this::convertToDto)
                .zipWith(monoLink, EntityModel::of);
    }

    @GetMapping("/pageable")
    public Mono<ResponseEntity<PageSupport<StudentDTO>>> getPage(
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

    @GetMapping("/sorted")
    public Mono<ResponseEntity<Flux<StudentDTO>>> findAllSorted(@RequestParam(name = "sort", defaultValue = "ASC") SortEnum sort) {

        Comparator<Student> comparator = sort.equals(SortEnum.ASC)
                ? Comparator.comparingInt(Student::getAge)
                : Comparator.comparingInt(Student::getAge).reversed();

        Flux<StudentDTO> fx = service.findAll().sort(comparator).map(this::convertToDto);

        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private StudentDTO convertToDto(Student model){
        return mapper.map(model, StudentDTO.class);
    }

    private Student convertToDocument(StudentDTO dto){
        return mapper.map(dto, Student.class);
    }
}
