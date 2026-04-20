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
 * PasswordRecoveryService - Servicio de Recuperación de Contraseña
 * 
 * ¿Qué es?: Es el componente encargado de gestionar el flujo de seguridad cuando 
 * un usuario olvida su contraseña y necesita restablecerla.
 * 
 * ¿Para qué sirve?: Permite que un usuario recupere el acceso a su cuenta de forma 
 * segura mediante un código de un solo uso enviado a su correo institucional.
 * 
 * ¿Cómo funciona?: 
 * - Genera un código aleatorio de 6 dígitos (SecureRandom).
 * - Envía un correo electrónico con un diseño HTML profesional.
 * - Valida que el código ingresado coincida con el guardado en la base de datos.
 * - Al restablecer, valida que la nueva contraseña cumpla con las políticas de 
 *   seguridad (mayúsculas, símbolos, longitud).
 * - Limpia bloqueos e intentos fallidos tras un restablecimiento exitoso.
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
     * Paso 1: Genera y envía el código de recuperación.
     */
    @Transactional
    public void enviarCodigo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una cuenta con ese correo"));

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new IllegalStateException("La cuenta se encuentra inactiva");
        }

        // Generar código aleatorio de 6 dígitos
        String codigo = generarCodigo();
        usuario.setRecoverPassword(codigo); // Guardar temporalmente en el usuario
        usuarioRepository.save(usuario);

        // Enviar el correo con formato HTML
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
     * Paso 2: Verifica que el código proporcionado por el usuario sea el correcto.
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
     * Paso 3: Cambia la contraseña por la nueva y limpia el estado de seguridad.
     */
    @Transactional
    public void restablecerPassword(String correo, String codigo, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una cuenta con ese correo"));

        // Validar que el código siga siendo válido
        if (usuario.getRecoverPassword() == null || !usuario.getRecoverPassword().equals(codigo)) {
            throw new IllegalArgumentException("El código de verificación es incorrecto o ha expirado");
        }

        // --- VALIDACIONES DE SEGURIDAD ---
        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!nuevaPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una letra mayúscula");
        }
        if (!nuevaPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un carácter especial");
        }

        // Actualizar y limpiar
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setRecoverPassword(null); // El código ya no sirve
        usuario.setIntentosFallidos(0);   // Desbloquear si estaba bloqueada
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);
    }

    /**
     * Genera un número aleatorio de 6 cifras.
     */
    private String generarCodigo() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasa si el código de recuperación es adivinado?
     *    - Respuesta: Es difícil porque se genera usando 'SecureRandom' (seguridad criptográfica) 
     *      y son 900,000 combinaciones posibles.
     * 
     * 2. ¿El código expira?
     *    - Respuesta: Actualmente no tiene tiempo de expiración por reloj, pero solo sirve 
     *      una vez. Una vez que se usa para cambiar la contraseña, se borra ('null').
     * 
     * 3. ¿Dónde está el botón de "Enviar Código"?
     *    - Respuesta: En la página [ForgotPassword.jsx] o en el modal [CambiarContrasenaModal.jsx]. 
     *      Esos componentes llaman a los endpoints que invocan a este servicio.
     * 
     * 4. ¿Qué pasa si el usuario solicita 10 códigos seguidos?
     *    - Respuesta: Cada vez que solicite uno, el código anterior se sobrescribe en la base 
     *      de datos, por lo que solo el último código recibido en su correo será válido.
     */
}

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
