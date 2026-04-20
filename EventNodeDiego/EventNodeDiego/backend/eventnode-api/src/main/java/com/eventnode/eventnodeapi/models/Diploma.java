package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Configuración de diploma por evento (plantilla JRXML en Base64, firma). Tabla {@code diplomas}. */
@Entity
@Table(name = "diplomas")
public class Diploma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diploma")
    private Integer idDiploma;

    @Column(name = "id_evento", nullable = false, unique = true)
    private Integer idEvento;

    @Column(name = "nombre_evento", nullable = false, length = 200)
    private String nombreEvento;

    @Column(name = "firma", nullable = false, length = 255)
    private String firma;

    @Column(name = "diseno", nullable = false, length = 255)
    private String diseno;

    @Lob
    @Column(name = "plantilla_pdf", columnDefinition = "LONGTEXT")
    private String plantillaPdf;

    @Lob
    @Column(name = "firma_imagen", columnDefinition = "LONGTEXT")
    private String firmaImagen;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('ACTIVO','ELIMINADO')")
    private String estado;

    public Diploma() {
    }

    public Integer getIdDiploma() {
        return idDiploma;
    }

    public void setIdDiploma(Integer idDiploma) {
        this.idDiploma = idDiploma;
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public void setNombreEvento(String nombreEvento) {
        this.nombreEvento = nombreEvento;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public String getDiseno() {
        return diseno;
    }

    public void setDiseno(String diseno) {
        this.diseno = diseno;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPlantillaPdf() {
        return plantillaPdf;
    }

    public void setPlantillaPdf(String plantillaPdf) {
        this.plantillaPdf = plantillaPdf;
    }

    public String getFirmaImagen() {
        return firmaImagen;
    }

    public void setFirmaImagen(String firmaImagen) {
        this.firmaImagen = firmaImagen;
    }
}
