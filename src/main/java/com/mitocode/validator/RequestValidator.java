package com.mitocode.validator;

import com.mitocode.exception.ValidationCustomException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> Mono<T> validate(T t){
        if(t == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        }

        Set<ConstraintViolation<T>> constraints = validator.validate(t);

        if(constraints == null || constraints.isEmpty()) {
            return Mono.just(t);
        }
        Map<String, String> details = constraints.stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage// Manejo de claves duplicadas
                ));
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("details", details);
        errorResponse.put("message", "Fallo en la validación");

        return Mono.error(new ValidationCustomException(HttpStatus.BAD_REQUEST, errorResponse));
    }
}
