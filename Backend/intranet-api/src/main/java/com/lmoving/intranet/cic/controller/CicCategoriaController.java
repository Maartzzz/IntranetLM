package com.lmoving.intranet.cic.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lmoving.intranet.cic.CicCategoria;
import com.lmoving.intranet.cic.service.CicCategoriaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/cic")
public class CicCategoriaController {

    private final CicCategoriaService categoriaService;

    public CicCategoriaController(
            CicCategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // Categorías activas visibles para usuarios autenticados

    @GetMapping("/categorias")
    public ResponseEntity<List<CicCategoria>> listarActivas() {
        return ResponseEntity.ok(categoriaService.listarActivas());
    }

    // Administración exclusiva para ADMIN

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/categorias")
    public ResponseEntity<List<CicCategoria>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categorias")
    public ResponseEntity<CicCategoria> crear(
            @Valid @RequestBody CategoriaRequest request) {

        CicCategoria creada = categoriaService.crear(
                request.nombre(),
                request.descripcion());

        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/categorias/{id}")
    public ResponseEntity<CicCategoria> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {

        CicCategoria actualizada = categoriaService.actualizar(
                id,
                request.nombre(),
                request.descripcion());

        return ResponseEntity.ok(actualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/categorias/{id}/estado")
    public ResponseEntity<CicCategoria> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoCategoriaRequest request) {

        CicCategoria actualizada = categoriaService.cambiarEstado(
                id,
                request.activa());

        return ResponseEntity.ok(actualizada);
    }

    public record CategoriaRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
            String nombre,

            @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
            String descripcion) {
    }

    public record EstadoCategoriaRequest(
            @NotBlank(message = "El estado es obligatorio")
            String estado) {

        public boolean activa() {
            if ("ACTIVA".equalsIgnoreCase(estado)) {
                return true;
            }

            if ("INACTIVA".equalsIgnoreCase(estado)) {
                return false;
            }

            throw new IllegalArgumentException(
                    "El estado debe ser ACTIVA o INACTIVA");
        }
    }
}
