package com.lmoving.intranet.cic.dto;

import java.time.LocalDateTime;

import com.lmoving.intranet.cic.CicDocumento;

public class CicDocumentoResponse {

    private Long id;
    private String titulo;
    private String descripcion;
    private Long categoriaId;
    private String categoriaNombre;
    private String nombreArchivo;
    private String tipoMime;
    private Long tamanoBytes;
    private String estado;
    private String creadoPor;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private LocalDateTime publicadoEn;

    public CicDocumentoResponse() {
    }

    public static CicDocumentoResponse fromEntity(CicDocumento documento) {
        CicDocumentoResponse response = new CicDocumentoResponse();

        response.setId(documento.getId());
        response.setTitulo(documento.getTitulo());
        response.setDescripcion(documento.getDescripcion());
        response.setCategoriaId(documento.getCategoria().getId());
        response.setCategoriaNombre(documento.getCategoria().getNombre());
        response.setNombreArchivo(documento.getNombreArchivo());
        response.setTipoMime(documento.getTipoMime());
        response.setTamanoBytes(documento.getTamanoBytes());
        response.setEstado(documento.getEstado().name());
        response.setCreadoPor(documento.getCreadoPor().getUsername());
        response.setCreadoEn(documento.getCreadoEn());
        response.setActualizadoEn(documento.getActualizadoEn());
        response.setPublicadoEn(documento.getPublicadoEn());

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public LocalDateTime getPublicadoEn() {
        return publicadoEn;
    }

    public void setPublicadoEn(LocalDateTime publicadoEn) {
        this.publicadoEn = publicadoEn;
    }
}
