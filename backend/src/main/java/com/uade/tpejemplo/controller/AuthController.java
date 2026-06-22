package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.dto.request.LoginRequest;
import com.uade.tpejemplo.dto.request.RegisterRequest;
import com.uade.tpejemplo.dto.response.AuthResponse;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.model.Rol;
import com.uade.tpejemplo.model.Usuario;
import com.uade.tpejemplo.repository.UsuarioRepository;
import com.uade.tpejemplo.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El usuario '" + request.getUsername() + "' ya existe");
        }

        Usuario usuario = Usuario.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .rol(Rol.USER)
            .puedeAnularCredito(false)
            .puedeAnularCobranza(false)
            .build();

        usuarioRepository.save(usuario);

        String token = jwtUtil.generarToken(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse.from(token, usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generarToken(userDetails);

        Usuario usuario = (Usuario) userDetails;
        return ResponseEntity.ok(AuthResponse.from(token, usuario));
    }
}
