package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.models.Diploma;
import com.eventnode.eventnodeapi.models.DiplomaEmitido;
import com.eventnode.eventnodeapi.models.Asistencia;
import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.DiplomaRepository;
import com.eventnode.eventnodeapi.repositories.DiplomaEmitidoRepository;
import com.eventnode.eventnodeapi.repositories.AsistenciaRepository;
import com.eventnode.eventnodeapi.repositories.EventoRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import jakarta.mail.internet.MimeMessage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.JREmptyDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Integración JasperReports + JavaMail: la plantilla es JRXML codificado en Base64 (posible prefijo {@code data:}).
 * <p>Parámetros típicos del reporte: {@code STUDENT_NAME}, {@code EVENT_NAME}, {@code FIRMA_NOMBRE}, {@code FECHA},
 * stream {@code FIRMA_IMAGEN}. Se fuerzan propiedades del motor para fuentes PDF cuando faltan TTF en el classpath.</p>
 */
@Service
public class DiplomaService {

    private static final Logger log = LoggerFactory.getLogger(DiplomaService.class);

    private final DiplomaRepository diplomaRepository;
    private final DiplomaEmitidoRepository diplomaEmitidoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JavaMailSender mailSender;

    public DiplomaService(DiplomaRepository diplomaRepository,
                         DiplomaEmitidoRepository diplomaEmitidoRepository,
                         AsistenciaRepository asistenciaRepository,
                         EventoRepository eventoRepository,
                         UsuarioRepository usuarioRepository,
                         JavaMailSender mailSender) {
        this.diplomaRepository = diplomaRepository;
        this.diplomaEmitidoRepository = diplomaEmitidoRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.mailSender = mailSender;
    }

    /**
     * Crea configuración de diploma para un evento. Un evento solo puede tener un registro en {@code diplomas}.
     *
     * @param plantillaPdf JRXML en Base64; se valida compilando con Jasper antes de guardar
     * @param firmaImagen  imagen de firma (también Base64 / data URL) requerida por la plantilla
     */
    @Transactional
    public void crearDiploma(Integer idEvento, String firma, String diseno,
                             String plantillaPdf, String firmaImagen) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (diplomaRepository.findByIdEvento(idEvento).isPresent()) {
            throw new IllegalStateException("Ya existe un diploma para este evento");
        }

        validarPlantillaJasper(plantillaPdf);

        Diploma diploma = new Diploma();
        diploma.setIdEvento(idEvento);
        diploma.setNombreEvento(evento.getNombre());
        diploma.setFirma(firma.isBlank() ? "Administrador" : firma);
        diploma.setDiseno(diseno.isBlank() ? "Personalizado" : diseno);
        diploma.setPlantillaPdf(plantillaPdf);
        diploma.setFirmaImagen(firmaImagen);
        diploma.setFechaCreacion(LocalDateTime.now());
        diploma.setEstado("ACTIVO");

        diplomaRepository.save(diploma);
    }

    /** Panel administración: diplomas ACTIVO con métricas de emisión vs asistencias del evento. */
    public List<Map<String, Object>> listarDiplomas() {
        List<Diploma> diplomas = diplomaRepository.findByEstado("ACTIVO");

        return diplomas.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idDiploma", d.getIdDiploma());
            map.put("idEvento", d.getIdEvento());
            map.put("nombreEvento", d.getNombreEvento());
            map.put("firma", d.getFirma());
            map.put("diseno", d.getDiseno());
            map.put("fechaCreacion", d.getFechaCreacion());
            map.put("tienePlantilla", d.getPlantillaPdf() != null && !d.getPlantillaPdf().isBlank());
            map.put("tieneFirma", d.getFirmaImagen() != null && !d.getFirmaImagen().isBlank());

            long totalEmitidos = diplomaEmitidoRepository.countByIdDiploma(d.getIdDiploma());
            map.put("totalEmitidos", totalEmitidos);

            long countAsistencias = asistenciaRepository.countByIdEvento(d.getIdEvento());
            long totalPendientes = countAsistencias - totalEmitidos;
            map.put("totalPendientes", totalPendientes);

            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Por cada asistencia del evento sin registro previo en {@code diplomas_emitidos}, genera PDF,
     * intenta enviar por SMTP y persiste ENVIADO o ERROR. No revierte PDFs ya enviados si uno falla después.
     *
     * @return mapa con totales y opcionalmente {@code primerError} para diagnóstico rápido en UI
     */
    @Transactional
    public Map<String, Object> emitirDiplomas(Integer idDiploma) {
        Diploma diploma = diplomaRepository.findById(idDiploma)
                .orElseThrow(() -> new IllegalArgumentException("Diploma no encontrado"));

        if (diploma.getPlantillaPdf() == null || diploma.getPlantillaPdf().isBlank()) {
            throw new IllegalArgumentException("El diploma no tiene una plantilla PDF configurada");
        }

        List<Asistencia> asistencias = asistenciaRepository.findByIdEvento(diploma.getIdEvento());

        if (asistencias.isEmpty()) {
            throw new IllegalStateException("No hay asistencias registradas para este evento");
        }

        long enviados = 0;
        long errores = 0;
        String primerError = null;
        List<DiplomaEmitido> existentes = diplomaEmitidoRepository.findByIdDiploma(idDiploma);

        for (Asistencia asistencia : asistencias) {
            boolean yaEmitido = existentes.stream()
                    .anyMatch(de -> de.getIdUsuario().equals(asistencia.getIdUsuario()));

            if (!yaEmitido) {
                Usuario usuario = usuarioRepository.findById(asistencia.getIdUsuario()).orElse(null);
                if (usuario == null) continue;

                String fullName = buildFullName(usuario);

                DiplomaEmitido diplomaEmitido = new DiplomaEmitido();
                diplomaEmitido.setIdDiploma(idDiploma);
                diplomaEmitido.setIdUsuario(asistencia.getIdUsuario());
                diplomaEmitido.setFechaEnvio(LocalDateTime.now());

                try {
                    byte[] pdfBytes = generarDiplomaPdf(diploma, fullName);
                    enviarCorreoDiploma(usuario.getCorreo(), fullName, diploma.getNombreEvento(), pdfBytes);
                    diplomaEmitido.setEstadoEnvio("ENVIADO");
                    enviados++;
                } catch (Exception e) {
                    log.error("Error al emitir diploma para usuario {} ({}): {}", usuario.getIdUsuario(), fullName, e.getMessage(), e);
                    diplomaEmitido.setEstadoEnvio("ERROR");
                    if (primerError == null) primerError = e.getMessage();
                    errores++;
                }

                diplomaEmitidoRepository.save(diplomaEmitido);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalEnviados", enviados);
        result.put("totalErrores", errores);
        if (primerError != null) result.put("primerError", primerError);
        return result;
    }

    /**
     * Descarga on-demand: mismo pipeline que emisión masiva pero para un par diploma–usuario.
     */
    public byte[] generarDiplomaPdf(Integer idDiploma, Integer idUsuario) {
        Diploma diploma = diplomaRepository.findById(idDiploma)
                .orElseThrow(() -> new IllegalArgumentException("Diploma no encontrado"));

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String fullName = buildFullName(usuario);
        return generarDiplomaPdf(diploma, fullName);
    }

    /**
     * Decodifica un JRXML en base64 (con o sin prefijo data:),
     * elimina el BOM UTF-8 (U+FEFF) y espacios iniciales que rompen el parser XML.
     */
    private byte[] decodeJrxml(String base64OrDataUrl) {
        String base64 = base64OrDataUrl.contains(",")
                ? base64OrDataUrl.substring(base64OrDataUrl.indexOf(",") + 1)
                : base64OrDataUrl;
        byte[] raw = Base64.getDecoder().decode(base64);

        // Convertir a String UTF-8 para limpiar BOM (U+FEFF) y whitespace inicial
        String content = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
        // Eliminar BOM y cualquier carácter de espacio/control antes de '<?xml'
        int start = 0;
        while (start < content.length() && (content.charAt(start) == '\uFEFF' || content.charAt(start) <= ' ')) {
            start++;
        }
        if (start > 0) {
            content = content.substring(start);
        }
        return content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Compila el JRXML una sola vez para fallar rápido si el XML es inválido (antes de guardar en BD). */
    private void validarPlantillaJasper(String plantillaBase64) {
        try {
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            System.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");
            System.setProperty("net.sf.jasperreports.default.font.name", "SansSerif");

            byte[] jrxmlBytes = decodeJrxml(plantillaBase64);
            JasperCompileManager.compileReport(new ByteArrayInputStream(jrxmlBytes));
        } catch (JRException e) {
            throw new IllegalArgumentException("La plantilla Jasper no es válida: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo procesar la plantilla: " + e.getMessage());
        }
    }

    /**
     * Pipeline Jasper: compile → fill con {@link JREmptyDataSource} (sin banda de datos SQL) → export PDF bytes.
     */
    private byte[] generarDiplomaPdf(Diploma diploma, String studentName) {
        try {
            // Fuentes DejaVu no disponibles en fat JAR — usar fuentes PDF estándar como fallback
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            System.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");
            System.setProperty("net.sf.jasperreports.default.font.name", "SansSerif");

            byte[] jrxmlBytes = decodeJrxml(diploma.getPlantillaPdf());

            JasperReport jasperReport = JasperCompileManager.compileReport(
                    new ByteArrayInputStream(jrxmlBytes));

            Map<String, Object> params = new HashMap<>();
            params.put("STUDENT_NAME", studentName);
            params.put("EVENT_NAME", diploma.getNombreEvento());
            params.put("FIRMA_NOMBRE", diploma.getFirma() != null ? diploma.getFirma() : "");

            Evento evento = eventoRepository.findById(diploma.getIdEvento()).orElse(null);
            if (evento != null && evento.getFechaInicio() != null) {
                String fecha = evento.getFechaInicio().format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "MX")));
                params.put("FECHA", fecha);
            } else {
                params.put("FECHA", "");
            }

            if (diploma.getFirmaImagen() != null && !diploma.getFirmaImagen().isBlank()) {
                String sigBase64 = diploma.getFirmaImagen();
                if (sigBase64.contains(",")) {
                    sigBase64 = sigBase64.substring(sigBase64.indexOf(",") + 1);
                }
                byte[] sigBytes = Base64.getDecoder().decode(sigBase64);
                params.put("FIRMA_IMAGEN", new ByteArrayInputStream(sigBytes));
            }

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, new JREmptyDataSource());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, baos);

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF del diploma: " + e.getMessage(), e);
        }
    }

    /** Correo de primera emisión con adjunto application/pdf. */
    private void enviarCorreoDiploma(String correo, String nombre, String nombreEvento, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("caszn06@gmail.com");
            helper.setTo(correo);
            helper.setSubject("¡Has recibido un diploma! - " + nombreEvento);
            helper.setText(buildDiplomaEmailHtml(nombre, nombreEvento), true);
            helper.addAttachment("Diploma_" + nombreEvento.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf",
                    new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo: " + e.getMessage(), e);
        }
    }

    /** Mismo adjunto PDF tras cambiar plantilla o firma en panel admin. */
    private void enviarCorreoDiplomaActualizado(String correo, String nombre, String nombreEvento, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("caszn06@gmail.com");
            helper.setTo(correo);
            helper.setSubject("Tu diploma ha sido actualizado - " + nombreEvento);
            helper.setText(buildDiplomaUpdatedEmailHtml(nombre, nombreEvento), true);
            helper.addAttachment("Diploma_" + nombreEvento.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf",
                    new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo de actualización: " + e.getMessage(), e);
        }
    }

    private String buildDiplomaUpdatedEmailHtml(String nombre, String nombreEvento) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f5f6fa;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f5f6fa;padding:40px 20px;'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>" +
                "<tr><td style='background:linear-gradient(135deg,#F59E0B 0%,#D97706 100%);padding:40px 30px;text-align:center;'>" +
                "<h1 style='color:#ffffff;margin:0 0 8px 0;font-size:28px;'>&#128221; Diploma Actualizado</h1>" +
                "<p style='color:rgba(255,255,255,0.9);margin:0;font-size:16px;'>Tu diploma ha sido modificado</p>" +
                "</td></tr>" +
                "<tr><td style='padding:40px 30px;'>" +
                "<p style='font-size:16px;color:#333;margin:0 0 20px 0;'>Hola <strong>" + nombre + "</strong>,</p>" +
                "<p style='font-size:15px;color:#555;line-height:1.6;margin:0 0 24px 0;'>" +
                "Te informamos que el diploma del siguiente evento ha sido actualizado:</p>" +
                "<div style='background-color:#FFF7ED;border-left:4px solid #F59E0B;border-radius:8px;padding:16px 20px;margin:0 0 24px 0;'>" +
                "<p style='margin:0;font-size:18px;font-weight:bold;color:#D97706;'>" + nombreEvento + "</p>" +
                "</div>" +
                "<p style='font-size:15px;color:#555;line-height:1.6;margin:0 0 8px 0;'>" +
                "Te adjuntamos la nueva versión de tu diploma en formato PDF. También puedes descargarlo desde la aplicación de EventNode.</p>" +
                "</td></tr>" +
                "<tr><td style='background-color:#f8f9fb;padding:24px 30px;text-align:center;border-top:1px solid #eee;'>" +
                "<p style='margin:0;font-size:13px;color:#999;'>EventNode — Sistema de Gestión de Eventos</p>" +
                "</td></tr>" +
                "</table></td></tr></table></body></html>";
    }

    private String buildDiplomaEmailHtml(String nombre, String nombreEvento) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f5f6fa;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f5f6fa;padding:40px 20px;'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>" +
                // Header
                "<tr><td style='background:linear-gradient(135deg,#1A56DB 0%,#3B82F6 100%);padding:40px 30px;text-align:center;'>" +
                "<h1 style='color:#ffffff;margin:0 0 8px 0;font-size:28px;'>&#127891; ¡Felicidades!</h1>" +
                "<p style='color:rgba(255,255,255,0.9);margin:0;font-size:16px;'>Has recibido un diploma</p>" +
                "</td></tr>" +
                // Body
                "<tr><td style='padding:40px 30px;'>" +
                "<p style='font-size:16px;color:#333;margin:0 0 20px 0;'>Hola <strong>" + nombre + "</strong>,</p>" +
                "<p style='font-size:15px;color:#555;line-height:1.6;margin:0 0 24px 0;'>" +
                "Nos complace informarte que has recibido tu diploma por tu participación en el evento:</p>" +
                "<div style='background-color:#F0F7FF;border-left:4px solid #1A56DB;border-radius:8px;padding:16px 20px;margin:0 0 24px 0;'>" +
                "<p style='margin:0;font-size:18px;font-weight:bold;color:#1A56DB;'>" + nombreEvento + "</p>" +
                "</div>" +
                "<p style='font-size:15px;color:#555;line-height:1.6;margin:0 0 8px 0;'>" +
                "Tu diploma se encuentra adjunto a este correo en formato PDF. También puedes descargarlo desde la aplicación de EventNode.</p>" +
                "</td></tr>" +
                // Footer
                "<tr><td style='background-color:#f8f9fb;padding:24px 30px;text-align:center;border-top:1px solid #eee;'>" +
                "<p style='margin:0;font-size:13px;color:#999;'>EventNode — Sistema de Gestión de Eventos</p>" +
                "</td></tr>" +
                "</table></td></tr></table></body></html>";
    }

    /**
     * Actualización parcial; luego reenvía PDF a todos los que ya tenían fila en {@code diplomas_emitidos}
     * (independientemente de si antes hubo ERROR — se reintenta).
     */
    @Transactional
    public void actualizarDiploma(Integer idDiploma, String firma, String diseno,
                                   String plantillaPdf, String firmaImagen) {
        Diploma diploma = diplomaRepository.findById(idDiploma)
                .orElseThrow(() -> new IllegalArgumentException("Diploma no encontrado"));

        if (firma != null && !firma.isBlank()) diploma.setFirma(firma);
        if (diseno != null && !diseno.isBlank()) diploma.setDiseno(diseno);
        if (plantillaPdf != null && !plantillaPdf.isBlank()) {
            validarPlantillaJasper(plantillaPdf);
            diploma.setPlantillaPdf(plantillaPdf);
        }
        if (firmaImagen != null && !firmaImagen.isBlank()) diploma.setFirmaImagen(firmaImagen);

        diplomaRepository.save(diploma);

        // Re-generate and re-send PDFs to all recipients who already received the diploma
        List<DiplomaEmitido> emitidos = diplomaEmitidoRepository.findByIdDiploma(idDiploma);
        for (DiplomaEmitido de : emitidos) {
            Usuario usuario = usuarioRepository.findById(de.getIdUsuario()).orElse(null);
            if (usuario == null) continue;

            String fullName = buildFullName(usuario);
            try {
                byte[] pdfBytes = generarDiplomaPdf(diploma, fullName);
                enviarCorreoDiplomaActualizado(usuario.getCorreo(), fullName, diploma.getNombreEvento(), pdfBytes);
                de.setEstadoEnvio("ENVIADO");
                de.setFechaEnvio(LocalDateTime.now());
            } catch (Exception e) {
                log.error("Error al re-emitir diploma actualizado para usuario {} ({}): {}", usuario.getIdUsuario(), fullName, e.getMessage(), e);
                de.setEstadoEnvio("ERROR");
            }
            diplomaEmitidoRepository.save(de);
        }
    }

    /** Borra emisiones hijas primero para no violar FK (si existe en esquema MySQL). */
    @Transactional
    public void eliminarDiploma(Integer idDiploma) {
        Diploma diploma = diplomaRepository.findById(idDiploma)
                .orElseThrow(() -> new IllegalArgumentException("Diploma no encontrado"));

        // Delete all related DiplomaEmitido records first
        List<DiplomaEmitido> emitidos = diplomaEmitidoRepository.findByIdDiploma(idDiploma);
        if (!emitidos.isEmpty()) {
            diplomaEmitidoRepository.deleteAll(emitidos);
        }

        // Hard delete the diploma from the database
        diplomaRepository.delete(diploma);
    }

    /** Vista alumno: diplomas recibidos (tabla emisión) con metadatos del diploma padre. */
    public List<Map<String, Object>> listarDiplomasEstudiante(Integer idUsuario) {
        List<DiplomaEmitido> diplomasEmitidos = diplomaEmitidoRepository.findByIdUsuario(idUsuario);

        return diplomasEmitidos.stream().map(de -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idEmitido", de.getIdEmitido());
            map.put("idDiploma", de.getIdDiploma());
            map.put("fechaEnvio", de.getFechaEnvio());
            map.put("estadoEnvio", de.getEstadoEnvio());

            Diploma diploma = diplomaRepository.findById(de.getIdDiploma()).orElse(null);
            if (diploma != null) {
                map.put("nombreEvento", diploma.getNombreEvento());
                map.put("diseno", diploma.getDiseno());
            }

            return map;
        }).collect(Collectors.toList());
    }

    /** Detalle admin: incluye lista anidada de destinatarios y estado de cada envío. */
    public Map<String, Object> obtenerDiploma(Integer idDiploma) {
        Diploma diploma = diplomaRepository.findById(idDiploma)
                .orElseThrow(() -> new IllegalArgumentException("Diploma no encontrado"));

        Map<String, Object> map = new HashMap<>();
        map.put("idDiploma", diploma.getIdDiploma());
        map.put("idEvento", diploma.getIdEvento());
        map.put("nombreEvento", diploma.getNombreEvento());
        map.put("firma", diploma.getFirma());
        map.put("diseno", diploma.getDiseno());
        map.put("fechaCreacion", diploma.getFechaCreacion());
        map.put("estado", diploma.getEstado());
        map.put("tienePlantilla", diploma.getPlantillaPdf() != null && !diploma.getPlantillaPdf().isBlank());
        map.put("tieneFirma", diploma.getFirmaImagen() != null && !diploma.getFirmaImagen().isBlank());
        map.put("firmaImagen", diploma.getFirmaImagen());

        List<DiplomaEmitido> emitidos = diplomaEmitidoRepository.findByIdDiploma(idDiploma);
        List<Map<String, Object>> emitidosList = emitidos.stream().map(de -> {
            Map<String, Object> emMap = new HashMap<>();
            emMap.put("idEmitido", de.getIdEmitido());
            emMap.put("idUsuario", de.getIdUsuario());
            emMap.put("fechaEnvio", de.getFechaEnvio());
            emMap.put("estadoEnvio", de.getEstadoEnvio());

            Usuario usuario = usuarioRepository.findById(de.getIdUsuario()).orElse(null);
            if (usuario != null) {
                emMap.put("nombre", buildFullName(usuario));
            }

            return emMap;
        }).collect(Collectors.toList());

        map.put("emitidos", emitidosList);

        return map;
    }

    /**
     * Render de prueba sin persistir: usa nombre de participante ficticio y PNG 1×1 si no hay firma
     * para que el parámetro stream del JRXML nunca sea null.
     */
    public byte[] previewPlantilla(String plantillaPdf, String eventName, String signerName, String firmaImagen) {
        try {
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            System.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");
            System.setProperty("net.sf.jasperreports.default.font.name", "SansSerif");

            byte[] jrxmlBytes = decodeJrxml(plantillaPdf);
            JasperReport jasperReport = JasperCompileManager.compileReport(new ByteArrayInputStream(jrxmlBytes));

            Map<String, Object> params = new HashMap<>();
            params.put("STUDENT_NAME", "Nombre del Participante");
            params.put("EVENT_NAME", eventName != null && !eventName.isBlank() ? eventName : "Nombre del Evento");
            params.put("FIRMA_NOMBRE", signerName != null && !signerName.isBlank() ? signerName : "Nombre del Firmante");
            params.put("FECHA", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "MX"))));

            if (firmaImagen != null && !firmaImagen.isBlank()) {
                String sigBase64 = firmaImagen.contains(",")
                        ? firmaImagen.substring(firmaImagen.indexOf(",") + 1)
                        : firmaImagen;
                byte[] sigBytes = Base64.getDecoder().decode(sigBase64);
                params.put("FIRMA_IMAGEN", new ByteArrayInputStream(sigBytes));
            } else {
                // PNG transparente 1×1 para evitar NullPointerException en el elemento <image> del JRXML
                byte[] transparentPng = Base64.getDecoder().decode(
                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");
                params.put("FIRMA_IMAGEN", new ByteArrayInputStream(transparentPng));
            }

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar preview: " + e.getMessage(), e);
        }
    }

    /** PDF de ejemplo usando plantilla ya guardada en un {@link Diploma} existente. */
    public byte[] previewDiploma(Integer idDiploma) {
        Diploma diploma = diplomaRepository.findById(idDiploma)
                .orElseThrow(() -> new IllegalArgumentException("Diploma no encontrado"));
        return generarDiplomaPdf(diploma, "Nombre del Participante");
    }

    /** Concatena nombre y apellidos para parámetros Jasper y asunto de correo. */
    private String buildFullName(Usuario usuario) {
        String fullName = usuario.getNombre() + " " + usuario.getApellidoPaterno();
        if (usuario.getApellidoMaterno() != null && !usuario.getApellidoMaterno().isEmpty()) {
            fullName += " " + usuario.getApellidoMaterno();
        }
        return fullName;
    }
}
