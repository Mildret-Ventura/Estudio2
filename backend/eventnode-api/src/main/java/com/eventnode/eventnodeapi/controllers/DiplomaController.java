package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.DiplomaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DiplomaController - Controlador de Gestión de Diplomas
 * 
 * ¿Qué es?: Es el controlador encargado de la creación, emisión y descarga de los 
 * reconocimientos (diplomas) otorgados a los alumnos por su asistencia.
 * 
 * ¿Para qué sirve?: Permite a los administradores diseñar plantillas de diplomas para cada 
 * evento y emitirlos masivamente a todos los que asistieron. También permite a los 
 * estudiantes ver y descargar sus diplomas ganados.
 * 
 * ¿Cómo funciona?: 
 * - Recibe imágenes de plantillas y firmas en formato Base64.
 * - Utiliza 'DiplomaService' para la lógica de generación de PDFs y envío de correos.
 * - Gestiona la emisión masiva ('/emitir') que recorre la lista de asistentes.
 * - Provee una vista previa ('/preview-template') para que el admin vea cómo quedará el diploma.
 */
@RestController
@RequestMapping("/api/diplomas")
public class DiplomaController {

    private final DiplomaService diplomaService;

    public DiplomaController(DiplomaService diplomaService) {
        this.diplomaService = diplomaService;
    }

    /**
     * Crea un nuevo diseño de diploma para un evento específico.
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearDiploma(@RequestBody Map<String, Object> body) {
        try {
            Integer idEvento = body.get("idEvento") != null ? Integer.parseInt(body.get("idEvento").toString()) : null;
            String firma = body.get("firma") != null ? body.get("firma").toString() : "";
            String diseno = body.get("diseno") != null ? body.get("diseno").toString() : "Personalizado";
            String plantillaPdf = body.get("plantillaPdf") != null ? body.get("plantillaPdf").toString() : null;
            String firmaImagen = body.get("firmaImagen") != null ? body.get("firmaImagen").toString() : null;

            // Validaciones básicas de campos obligatorios
            if (idEvento == null) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "idEvento es requerido");
                return ResponseEntity.badRequest().body(error);
            }

            if (plantillaPdf == null || plantillaPdf.isBlank()) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "La plantilla PDF es requerida");
                return ResponseEntity.badRequest().body(error);
            }

            if (firmaImagen == null || firmaImagen.isBlank()) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "La firma es requerida");
                return ResponseEntity.badRequest().body(error);
            }

            diplomaService.crearDiploma(idEvento, firma, diseno, plantillaPdf, firmaImagen);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Diploma creado exitosamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Lista todos los diseños de diplomas creados (Vista de Administrador).
     */
    @GetMapping("/")
    public ResponseEntity<?> listarDiplomas() {
        try {
            List<Map<String, Object>> diplomas = diplomaService.listarDiplomas();
            return ResponseEntity.ok(diplomas);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtiene los detalles de un diseño de diploma específico.
     */
    @GetMapping("/{idDiploma}")
    public ResponseEntity<?> obtenerDiploma(@PathVariable Integer idDiploma) {
        try {
            Map<String, Object> diploma = diplomaService.obtenerDiploma(idDiploma);
            return ResponseEntity.ok(diploma);
        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Proceso masivo: Genera y envía por correo los diplomas a todos los asistentes del evento.
     */
    @PostMapping("/{idDiploma}/emitir")
    public ResponseEntity<?> emitirDiplomas(@PathVariable Integer idDiploma) {
        try {
            Map<String, Object> result = diplomaService.emitirDiplomas(idDiploma);

            long enviados = ((Number) result.get("totalEnviados")).longValue();
            long errores = ((Number) result.get("totalErrores")).longValue();

            Map<String, Object> response = new HashMap<>();
            response.put("totalEmitidos", enviados);
            response.put("totalErrores", errores);

            if (errores > 0 && result.containsKey("primerError")) {
                response.put("primerError", result.get("primerError"));
            }

            if (enviados == 0 && errores > 0) {
                response.put("mensaje", "No se pudo enviar ningún diploma. Error: " + result.get("primerError"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("mensaje", "Diplomas procesados: " + enviados + " enviados, " + errores + " con error");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualiza un diseño de diploma.
     */
    @PutMapping("/{idDiploma}")
    public ResponseEntity<?> actualizarDiploma(@PathVariable Integer idDiploma, @RequestBody Map<String, Object> body) {
        try {
            String firma = body.get("firma") != null ? body.get("firma").toString() : null;
            String diseno = body.get("diseno") != null ? body.get("diseno").toString() : null;
            String plantillaPdf = body.get("plantillaPdf") != null ? body.get("plantillaPdf").toString() : null;
            String firmaImagen = body.get("firmaImagen") != null ? body.get("firmaImagen").toString() : null;

            diplomaService.actualizarDiploma(idDiploma, firma, diseno, plantillaPdf, firmaImagen);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Diploma actualizado exitosamente. Se notificó a todos los destinatarios.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Elimina un diseño de diploma del sistema.
     */
    @DeleteMapping("/{idDiploma}")
    public ResponseEntity<?> eliminarDiploma(@PathVariable Integer idDiploma) {
        try {
            diplomaService.eliminarDiploma(idDiploma);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Diploma eliminado exitosamente");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Lista todos los diplomas que un estudiante específico ha ganado.
     */
    @GetMapping("/estudiante/{idUsuario}")
    public ResponseEntity<?> listarDiplomasEstudiante(@PathVariable Integer idUsuario) {
        try {
            List<Map<String, Object>> diplomas = diplomaService.listarDiplomasEstudiante(idUsuario);
            return ResponseEntity.ok(diplomas);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si emito diplomas y un alumno no tiene correo registrado?
     *    - Respuesta: El sistema contará un error en 'totalErrores' y el servicio 
     *      lanzará una advertencia, pero continuará enviando el resto de los diplomas.
     * 
     * 2. ¿Cómo se genera el archivo PDF final?
     *    - Respuesta: La lógica no está aquí, sino en [DiplomaService.java]. Se utiliza 
     *      una librería (probablemente iText o OpenPDF) para sobreponer el nombre del 
     *      alumno sobre la 'plantillaPdf'.
     * 
     * 3. ¿Dónde está el botón para que el estudiante descargue su diploma?
     *    - Respuesta: En la página [StudentDiplomas.jsx]. Ese componente llama a 
     *      '/api/diplomas/estudiante/{id}' para obtener la lista de sus reconocimientos.
     * 
     * 4. ¿Qué es 'firmaImagen'?
     *    - Respuesta: Es la imagen de la firma del director o autoridad que avala el 
     *      evento, subida en formato Base64 para ser estampada en el PDF.
     */
}

    @PostMapping("/preview-template")
    public ResponseEntity<?> previewTemplate(@RequestBody Map<String, Object> body) {
        try {
            String plantillaPdf = body.get("plantillaPdf") != null ? body.get("plantillaPdf").toString() : null;
            String eventName    = body.get("eventName")    != null ? body.get("eventName").toString()    : null;
            String signerName   = body.get("signerName")   != null ? body.get("signerName").toString()   : null;
            String firmaImagen  = body.get("firmaImagen")  != null ? body.get("firmaImagen").toString()  : null;

            if (plantillaPdf == null || plantillaPdf.isBlank()) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "plantillaPdf es requerido");
                return ResponseEntity.badRequest().body(error);
            }

            byte[] pdfBytes = diplomaService.previewPlantilla(plantillaPdf, eventName, signerName, firmaImagen);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al generar previsualización: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{idDiploma}/preview")
    public ResponseEntity<?> previewDiploma(@PathVariable Integer idDiploma) {
        try {
            byte[] pdfBytes = diplomaService.previewDiploma(idDiploma);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al generar previsualización: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{idDiploma}/descargar/{idUsuario}")
    public ResponseEntity<?> descargarDiploma(@PathVariable Integer idDiploma, @PathVariable Integer idUsuario) {
        try {
            byte[] pdfBytes = diplomaService.generarDiplomaPdf(idDiploma, idUsuario);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "diploma.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al generar diploma: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
