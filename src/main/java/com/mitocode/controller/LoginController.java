package com.mitocode.controller;

import com.mitocode.model.Role;
import com.mitocode.model.User;
import com.mitocode.security.AuthRequest;
import com.mitocode.security.AuthResponse;
import com.mitocode.security.JwtUtil;
import com.mitocode.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final JwtUtil jwtUtil;
    private final IUserService service;

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody AuthRequest authRequest){
        return service.searchByUser(authRequest.getUsername())
                .map(userDetails -> {
                    if(BCrypt.checkpw(authRequest.getPassword(), userDetails.getPassword())){
                        String token = jwtUtil.generateToken(userDetails);
                        Date expiration = jwtUtil.getExpirationDateFromToken(token);

                        return ResponseEntity.ok(new AuthResponse(token, expiration));
                    }else{
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(@RequestBody User authRequest) {
        return service.searchByUser(authRequest.getUsername())
                .flatMap(existingUser -> Mono.just(ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new AuthResponse("El nombre de usuario ya existe", null))))
                .switchIfEmpty(Mono.defer(() -> {
                    // Encriptar la contraseña
                    String encryptedPassword = BCrypt.hashpw(authRequest.getPassword(), BCrypt.gensalt());

                    // Setear la contraseña encriptada
                    authRequest.setPassword(encryptedPassword);

                    // Guardar el usuario en la base de datos
                    return service.save(authRequest)
                            .map(savedUser -> {
                                List<String> roles = savedUser.getRoles().stream()
                                        .map(Role::getName)
                                        .toList();
                                com.mitocode.security.User user = new com.mitocode.security.User(savedUser.getUsername(), savedUser.getPassword(), true, roles);
                                // Genera el token JWT
                                String token = jwtUtil.generateToken(user);
                                Date expiration = jwtUtil.getExpirationDateFromToken(token);

                                // Retorna la respuesta con el token
                                return ResponseEntity.ok(new AuthResponse(token, expiration));
                            });
                }));
    }
}
