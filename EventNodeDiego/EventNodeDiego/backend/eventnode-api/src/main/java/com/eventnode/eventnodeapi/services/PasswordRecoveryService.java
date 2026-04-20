package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Flujo de tres pasos (enviar / verificar / restablecer) usando la columna {@code usuarios.recover_password}
 * como almacenamiento temporal del código. <strong>No hay expiración por tiempo</strong> en código actual:
 * el código se invalida solo al restablecer con éxito o al sobrescribir con un nuevo envío.
 */
@Service
public class PasswordRecoveryService {

    private final UsuarioRepository usuarioRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordRecoveryService(UsuarioRepository usuarioRepository,
                                   JavaMailSender mailSender,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Genera código pseudoaleatorio seguro, persiste en usuario y envía correo.
     * <p>Si el envío SMTP falla, el error solo se imprime en consola: el código ya quedó guardado
     * (el usuario podría no recibir el correo — mejora posible: transacción solo tras send exitoso).</p>
     */
    @Transactional
    public void enviarCodigo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una cuenta con ese correo"));

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new IllegalStateException("La cuenta se encuentra inactiva");
        }

        String codigo = generarCodigo();
        usuario.setRecoverPassword(codigo);
        usuarioRepository.save(usuario);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(correo);
            helper.setSubject("EventNode - Código de recuperación");
            helper.setText(buildHtmlEmail(usuario.getNombre(), codigo), true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo de recuperación: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error al enviar correo de recuperación: " + e.getMessage());
        }
    }

    /**
     * Comprueba coincidencia exacta del código sin modificar la contraseña ni limpiar el código.
     */
    @Transactional
    public void verificarCodigo(String correo, String codigo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una cuenta con ese correo"));

        if (usuario.getRecoverPassword() == null || usuario.getRecoverPassword().isBlank()) {
            throw new IllegalStateException("No hay un código de recuperación activo para esta cuenta");
        }

        if (!usuario.getRecoverPassword().equals(codigo)) {
            throw new IllegalArgumentException("El código de verificación es incorrecto");
        }
    }

    /**
     * Vuelve a validar el código, aplica reglas de complejidad a {@code nuevaPassword}, guarda BCrypt y limpia recuperación.
     */
    @Transactional
    public void restablecerPassword(String correo, String codigo, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una cuenta con ese correo"));

        if (usuario.getRecoverPassword() == null || !usuario.getRecoverPassword().equals(codigo)) {
            throw new IllegalArgumentException("El código de verificación es incorrecto o ha expirado");
        }

        // Validaciones de la nueva contraseña
        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!nuevaPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una letra mayúscula");
        }
        if (!nuevaPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un carácter especial");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setRecoverPassword(null);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);
    }

    /** Rango [100000, 999999] inclusive. */
    private String generarCodigo() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /** Plantilla HTML embebida para clientes de correo (tablas inline para compatibilidad). */
    private String buildHtmlEmail(String nombre, String codigo) {
        String[] digits = codigo.split("");
        StringBuilder digitBoxes = new StringBuilder();
        for (String d : digits) {
            digitBoxes.append(
                "<td style=\"width:44px;height:52px;background-color:#EBF2FF;border-radius:10px;" +
                "font-size:26px;font-weight:700;color:#1A56DB;text-align:center;vertical-align:middle;" +
                "font-family:'Segoe UI',Roboto,Arial,sans-serif;letter-spacing:0;border:2px solid #C6DAFE;\">" +
                d + "</td>"
            );
        }

        return "<!DOCTYPE html>" +
            "<html lang=\"es\"><head><meta charset=\"UTF-8\"/></head>" +
            "<body style=\"margin:0;padding:0;background-color:#F0F4FA;font-family:'Segoe UI',Roboto,Arial,sans-serif;\">" +
            "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color:#F0F4FA;padding:40px 0;\">" +
            "<tr><td align=\"center\">" +

            // Card container
            "<table role=\"presentation\" width=\"480\" cellspacing=\"0\" cellpadding=\"0\" " +
            "style=\"background-color:#FFFFFF;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);\">" +

            // Blue header bar
            "<tr><td style=\"background:linear-gradient(135deg,#1A56DB 0%,#3B82F6 100%);padding:32px 40px;text-align:center;\">" +
            "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tr>" +
            "<td align=\"center\">" +
            "<div style=\"width:56px;height:56px;background-color:rgba(255,255,255,0.2);border-radius:50%;" +
            "display:inline-block;line-height:56px;text-align:center;\">" +
            "<span style=\"font-size:28px;color:#FFFFFF;\">&#128274;</span>" +
            "</div>" +
            "</td></tr><tr><td align=\"center\" style=\"padding-top:16px;\">" +
            "<h1 style=\"margin:0;font-size:22px;font-weight:700;color:#FFFFFF;letter-spacing:-0.3px;\">Código de Verificación</h1>" +
            "</td></tr><tr><td align=\"center\" style=\"padding-top:6px;\">" +
            "<p style=\"margin:0;font-size:14px;color:rgba(255,255,255,0.85);\">Recuperación de contraseña</p>" +
            "</td></tr></table>" +
            "</td></tr>" +

            // Body content
            "<tr><td style=\"padding:36px 40px 20px;\">" +
            "<p style=\"margin:0 0 4px;font-size:15px;color:#6B7280;\">Hola,</p>" +
            "<p style=\"margin:0 0 24px;font-size:17px;font-weight:600;color:#111827;\">" + nombre + "</p>" +
            "<p style=\"margin:0 0 28px;font-size:14px;color:#6B7280;line-height:1.6;\">" +
            "Recibimos una solicitud para restablecer la contraseña de tu cuenta en <strong style=\"color:#1A56DB;\">EventNode</strong>. " +
            "Usa el siguiente código para continuar:" +
            "</p>" +
            "</td></tr>" +

            // Code digits
            "<tr><td align=\"center\" style=\"padding:0 40px 28px;\">" +
            "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\">" +
            "<tr style=\"\">" + digitBoxes.toString() +
            "</tr></table>" +
            "</td></tr>" +

            // Info box
            "<tr><td style=\"padding:0 40px 32px;\">" +
            "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" +
            "<tr><td style=\"background-color:#F9FAFB;border-radius:10px;padding:16px 20px;border-left:4px solid #1A56DB;\">" +
            "<p style=\"margin:0 0 6px;font-size:12px;font-weight:600;color:#1A56DB;text-transform:uppercase;letter-spacing:0.5px;\">Importante</p>" +
            "<p style=\"margin:0;font-size:13px;color:#6B7280;line-height:1.5;\">" +
            "Si no solicitaste este cambio, puedes ignorar este mensaje. Tu cuenta permanecerá segura." +
            "</p></td></tr></table>" +
            "</td></tr>" +

            // Divider
            "<tr><td style=\"padding:0 40px;\"><hr style=\"border:none;border-top:1px solid #E5E7EB;margin:0;\"/></td></tr>" +

            // Footer
            "<tr><td style=\"padding:24px 40px 32px;text-align:center;\">" +
            "<p style=\"margin:0 0 4px;font-size:14px;font-weight:600;color:#1A56DB;\">EventNode</p>" +
            "<p style=\"margin:0;font-size:12px;color:#9CA3AF;\">Sistema de Gestión de Eventos</p>" +
            "</td></tr>" +

            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
}
