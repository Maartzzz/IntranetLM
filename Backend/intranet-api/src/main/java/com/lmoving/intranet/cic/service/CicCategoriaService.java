package com.lmoving.intranet.cic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmoving.intranet.cic.CicCategoria;
import com.lmoving.intranet.cic.repository.CicCategoriaRepository;

@Service
public class CicCategoriaService {

    private final CicCategoriaRepository categoriaRepository;

    public CicCategoriaService(
            CicCategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CicCategoria> listarTodas() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<CicCategoria> listarActivas() {
        return categoriaRepository.findAllByOrderByNombreAsc()
                .stream()
                .filter(CicCategoria::isActiva)
                .toList();
    }

    @Transactional
    public CicCategoria crear(String nombre, String descripcion) {
        validarNombre(nombre);

        String nombreLimpio = nombre.trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombreLimpio)) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con ese nombre");
        }

        CicCategoria categoria = new CicCategoria();
        categoria.setNombre(nombreLimpio);
        categoria.setDescripcion(limpiarDescripcion(descripcion));
        categoria.setActiva(true);

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public CicCategoria actualizar(
            Long id,
            String nombre,
            String descripcion) {

        validarNombre(nombre);

        CicCategoria categoria = obtenerPorId(id);
        String nombreLimpio = nombre.trim();

        boolean nombreDuplicado = categoriaRepository
                .findAllByOrderByNombreAsc()
                .stream()
                .anyMatch(otra ->
                        !otra.getId().equals(id)
                        && otra.getNombre().equalsIgnoreCase(nombreLimpio));

        if (nombreDuplicado) {
            throw new IllegalArgumentException(
                    "Ya existe otra categoría con ese nombre");
        }

        categoria.setNombre(nombreLimpio);
        categoria.setDescripcion(limpiarDescripcion(descripcion));

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public CicCategoria cambiarEstado(Long id, boolean activa) {
        CicCategoria categoria = obtenerPorId(id);
        categoria.setActiva(activa);
        return categoriaRepository.save(categoria);
    }

    private CicCategoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la categoría solicitada"));
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio");
        }

        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres");
        }
    }

    private String limpiarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        String limpia = descripcion.trim();

        if (limpia.length() > 300) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar los 300 caracteres");
        }

        return limpia;
    }
}
