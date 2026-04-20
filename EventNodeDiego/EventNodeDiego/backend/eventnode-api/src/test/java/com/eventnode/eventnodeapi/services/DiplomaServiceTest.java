package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.models.Asistencia;
import com.eventnode.eventnodeapi.models.Diploma;
import com.eventnode.eventnodeapi.models.DiplomaEmitido;
import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.AsistenciaRepository;
import com.eventnode.eventnodeapi.repositories.DiplomaEmitidoRepository;
import com.eventnode.eventnodeapi.repositories.DiplomaRepository;
import com.eventnode.eventnodeapi.repositories.EventoRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest
class DiplomaServiceTest {

    private static String plantillaPdfBase64;
    private static String firmaPngBase64;

    private static DiplomaService instancia;

    private DiplomaRepository diplomaRepository;
    private DiplomaEmitidoRepository diplomaEmitidoRepository;
    private AsistenciaRepository asistenciaRepository;
    private EventoRepository eventoRepository;
    private UsuarioRepository usuarioRepository;
    private JavaMailSender mailSender;

    private Integer idEvento;
    private Integer idDiploma;
    private Integer idUsuario;

    private Evento evento;
    private Diploma diploma;
    private Usuario usuario;
    private Asistencia asistencia;

    private List<Map<String, Object>> listaMaps;
    private Map<String, Object> mapaResultado;
    private byte[] bytesResultado;
    private long longResultado;
    private boolean guardado;

    @BeforeAll
    public static void iniciarPlantillas() throws Exception {
        plantillaPdfBase64 = crearJrxmlMinimoBase64();
        // PNG 1x1 transparente
        firmaPngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
        instancia = null;
    }

    private static String crearJrxmlMinimoBase64() {
        String jrxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\"\n" +
                "              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "              xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\"\n" +
                "              name=\"diploma_test\" pageWidth=\"595\" pageHeight=\"842\"\n" +
                "              columnWidth=\"555\" leftMargin=\"20\" rightMargin=\"20\"\n" +
                "              topMargin=\"20\" bottomMargin=\"20\">\n" +
                "    <parameter name=\"STUDENT_NAME\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"EVENT_NAME\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"FIRMA_NOMBRE\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"FECHA\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"FIRMA_IMAGEN\" class=\"java.io.InputStream\"/>\n" +
                "    <detail>\n" +
                "        <band height=\"30\">\n" +
                "            <textField>\n" +
                "                <reportElement x=\"0\" y=\"0\" width=\"555\" height=\"30\"/>\n" +
                "                <textFieldExpression><![CDATA[$P{STUDENT_NAME}]]></textFieldExpression>\n" +
                "            </textField>\n" +
                "        </band>\n" +
                "    </detail>\n" +
                "</jasperReport>";
        return Base64.getEncoder().encodeToString(jrxml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @BeforeEach
    void resetearVariables() throws Exception {
        idEvento = 100;
        idDiploma = 50;
        idUsuario = 1;
        listaMaps = null;
        mapaResultado = null;
        bytesResultado = null;
        longResultado = 0;
        guardado = false;

        diplomaRepository = mock(DiplomaRepository.class);
        diplomaEmitidoRepository = mock(DiplomaEmitidoRepository.class);
        asistenciaRepository = mock(AsistenciaRepository.class);
        eventoRepository = mock(EventoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        instancia = new DiplomaService(
                diplomaRepository,
                diplomaEmitidoRepository,
                asistenciaRepository,
                eventoRepository,
                usuarioRepository,
                mailSender
        );

        evento = new Evento();
        evento.setIdEvento(idEvento);
        evento.setNombre("Curso Test");

        diploma = new Diploma();
        diploma.setIdDiploma(idDiploma);
        diploma.setIdEvento(idEvento);
        diploma.setNombreEvento("Curso Test");
        diploma.setFirma("Director");
        diploma.setDiseno("Clasico");
        diploma.setPlantillaPdf(plantillaPdfBase64);
        diploma.setFirmaImagen(firmaPngBase64);
        diploma.setEstado("ACTIVO");

        usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre("Luis");
        usuario.setApellidoPaterno("Martinez");
        usuario.setCorreo("luis@test.com");

        asistencia = new Asistencia();
        asistencia.setIdUsuario(idUsuario);
        asistencia.setIdEvento(idEvento);
    }

    @Test
    void generarConstanciaPdfDatosValidosTest() throws Exception {
        // Preparar datos
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        // Invocar método
        bytesResultado = instancia.generarDiplomaPdf(idDiploma, idUsuario);

        // Validar resultado (formato PDF)
        assertNotNull(bytesResultado);
        assertTrue(bytesResultado.length > 4);
        assertEquals('%', (char) bytesResultado[0]);
        assertEquals('P', (char) bytesResultado[1]);
        assertEquals('D', (char) bytesResultado[2]);
        assertEquals('F', (char) bytesResultado[3]);
    }

    @Test
    void generarConstanciaPdfConPrefijoDataUrlTest() throws Exception {
        // Preparar datos
        diploma.setPlantillaPdf("data:application/pdf;base64," + plantillaPdfBase64);
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        // Invocar método
        bytesResultado = instancia.generarDiplomaPdf(idDiploma, idUsuario);

        // Validar resultado
        assertNotNull(bytesResultado);
        assertTrue(bytesResultado.length > 10);
    }

    @Test
    void generarConstanciaPdfNombreConCaracteresEspecialesTest() throws Exception {
        // Preparar datos (sanitizeForFont reemplaza caracteres > 255)
        usuario.setNombre("Test");
        usuario.setApellidoPaterno("\uD83D\uDE00"); // emoji
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        // Invocar método
        bytesResultado = instancia.generarDiplomaPdf(idDiploma, idUsuario);

        // Validar resultado
        assertNotNull(bytesResultado);
    }

    @Test
    void rechazoGenerarConstanciaPlantillaInvalidaTest() {
        // Preparar datos
        diploma.setPlantillaPdf("!!!no-es-base64-valido!!!");
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        // Invocar método
        assertThrows(RuntimeException.class, () -> instancia.generarDiplomaPdf(idDiploma, idUsuario));
    }

    @Test
    void crearDiplomaExitosoTest() {
        // Preparar datos
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));
        when(diplomaRepository.findByIdEvento(idEvento)).thenReturn(Optional.empty());
        when(diplomaRepository.save(any(Diploma.class))).thenAnswer(inv -> {
            guardado = true;
            return inv.getArgument(0);
        });

        // Invocar método
        instancia.crearDiploma(idEvento, "Firma", "Diseno", plantillaPdfBase64, firmaPngBase64);

        // Validar resultado
        assertTrue(guardado);
    }

    @Test
    void rechazoCrearDiplomaEventoNoEncontradoTest() {
        // Preparar datos
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.empty());

        // Invocar método
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> instancia.crearDiploma(idEvento, "F", "D", plantillaPdfBase64, firmaPngBase64));

        // Validar resultado
        assertEquals("Evento no encontrado", ex.getMessage());
    }

    @Test
    void rechazoCrearDiplomaDuplicadoTest() {
        // Preparar datos
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));
        when(diplomaRepository.findByIdEvento(idEvento)).thenReturn(Optional.of(new Diploma()));

        // Invocar método
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> instancia.crearDiploma(idEvento, "F", "D", plantillaPdfBase64, firmaPngBase64));

        // Validar resultado
        assertTrue(ex.getMessage().contains("Ya existe un diploma"));
    }

    @Test
    void listarDiplomasVaciosTest() {
        // Preparar datos
        when(diplomaRepository.findByEstado("ACTIVO")).thenReturn(Collections.emptyList());

        // Invocar método
        listaMaps = instancia.listarDiplomas();

        // Validar resultado
        assertNotNull(listaMaps);
        assertTrue(listaMaps.isEmpty());
    }

    @Test
    void listarDiplomasConTotalesTest() {
        // Preparar datos
        when(diplomaRepository.findByEstado("ACTIVO")).thenReturn(List.of(diploma));
        when(diplomaEmitidoRepository.countByIdDiploma(idDiploma)).thenReturn(2L);
        when(asistenciaRepository.countByIdEvento(idEvento)).thenReturn(5L);

        // Invocar método
        listaMaps = instancia.listarDiplomas();

        // Validar resultado
        assertEquals(1, listaMaps.size());
        assertEquals(2L, listaMaps.get(0).get("totalEmitidos"));
        assertEquals(3L, listaMaps.get(0).get("totalPendientes"));
    }

    @Test
    void rechazoEmitirSinPlantillaTest() {
        // Preparar datos
        diploma.setPlantillaPdf("");
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));

        // Invocar método
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> instancia.emitirDiplomas(idDiploma));

        // Validar resultado
        assertTrue(ex.getMessage().contains("plantilla PDF"));
    }

    @Test
    void rechazoEmitirConstanciaAlumnoSinAsistenciasRegistradasTest() {
        // Preparar datos (no hay filas de asistencia = no cumple requisito para emisión masiva)
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(asistenciaRepository.findByIdEvento(idEvento)).thenReturn(Collections.emptyList());
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(Collections.emptyList());

        // Invocar método
        assertThrows(IllegalStateException.class, () -> instancia.emitirDiplomas(idDiploma));

        // Validar resultado
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void emitirDiplomasConAsistenciaYCorreoOkTest() throws Exception {
        // Preparar datos
        diploma.setPlantillaPdf(plantillaPdfBase64);
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(asistenciaRepository.findByIdEvento(idEvento)).thenReturn(List.of(asistencia));
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(Collections.emptyList());
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(diplomaEmitidoRepository.save(any(DiplomaEmitido.class))).thenAnswer(inv -> inv.getArgument(0));

        // Invocar método
        longResultado = (Long) instancia.emitirDiplomas(idDiploma).get("totalEnviados");

        // Validar resultado
        assertEquals(1L, longResultado);
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void emitirDiplomasUsuarioInexistenteNoCuentaTest() {
        // Preparar datos
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(asistenciaRepository.findByIdEvento(idEvento)).thenReturn(List.of(asistencia));
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(Collections.emptyList());
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        // Invocar método
        longResultado = (Long) instancia.emitirDiplomas(idDiploma).get("totalEnviados");

        // Validar resultado
        assertEquals(0L, longResultado);
    }

    @Test
    void emitirDiplomasYaEmitidoOmiteDuplicadoTest() {
        // Preparar datos
        DiplomaEmitido ya = new DiplomaEmitido();
        ya.setIdUsuario(idUsuario);
        ya.setIdDiploma(idDiploma);
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(asistenciaRepository.findByIdEvento(idEvento)).thenReturn(List.of(asistencia));
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(List.of(ya));

        // Invocar método
        longResultado = (Long) instancia.emitirDiplomas(idDiploma).get("totalEnviados");

        // Validar resultado
        assertEquals(0L, longResultado);
    }

    @Test
    void rechazoConsultarConstanciaInexistenteTest() {
        // Preparar datos
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.empty());

        // Invocar método
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> instancia.obtenerDiploma(idDiploma));

        // Validar resultado
        assertEquals("Diploma no encontrado", ex.getMessage());
    }

    @Test
    void consultarConstanciaPreviamenteGeneradaTest() {
        // Preparar datos
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(Collections.emptyList());

        // Invocar método
        mapaResultado = instancia.obtenerDiploma(idDiploma);

        // Validar resultado
        assertEquals(idDiploma, mapaResultado.get("idDiploma"));
        assertEquals("Curso Test", mapaResultado.get("nombreEvento"));
        assertNotNull(mapaResultado.get("emitidos"));
    }

    @Test
    void listarConstanciasEstudianteVacioTest() {
        // Preparar datos
        when(diplomaEmitidoRepository.findByIdUsuario(idUsuario)).thenReturn(Collections.emptyList());

        // Invocar método
        listaMaps = instancia.listarDiplomasEstudiante(idUsuario);

        // Validar resultado
        assertTrue(listaMaps.isEmpty());
    }

    @Test
    void listarConstanciasEstudianteConDatosTest() {
        // Preparar datos
        DiplomaEmitido de = new DiplomaEmitido();
        de.setIdEmitido(1);
        de.setIdDiploma(idDiploma);
        de.setIdUsuario(idUsuario);
        de.setEstadoEnvio("ENVIADO");
        when(diplomaEmitidoRepository.findByIdUsuario(idUsuario)).thenReturn(List.of(de));
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));

        // Invocar método
        listaMaps = instancia.listarDiplomasEstudiante(idUsuario);

        // Validar resultado
        assertEquals(1, listaMaps.size());
        assertEquals("Curso Test", listaMaps.get(0).get("nombreEvento"));
    }

    @Test
    void actualizarDiplomaSinEmitidosPreviosTest() {
        // Preparar datos
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(Collections.emptyList());
        when(diplomaRepository.save(any(Diploma.class))).thenReturn(diploma);

        // Invocar método
        instancia.actualizarDiploma(idDiploma, "Nueva firma", null, null, null);

        // Validar resultado
        assertEquals("Nueva firma", diploma.getFirma());
    }

    @Test
    void actualizarDiplomaConEmitidosReenviaCorreoTest() throws Exception {
        // Preparar datos
        DiplomaEmitido de = new DiplomaEmitido();
        de.setIdUsuario(idUsuario);
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(List.of(de));
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(diplomaEmitidoRepository.save(any(DiplomaEmitido.class))).thenReturn(de);
        when(diplomaRepository.save(any(Diploma.class))).thenReturn(diploma);

        // Invocar método
        instancia.actualizarDiploma(idDiploma, null, "Nuevo diseno", null, null);

        // Validar resultado
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void eliminarDiplomaConEmitidosTest() {
        // Preparar datos
        DiplomaEmitido de = new DiplomaEmitido();
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(List.of(de));

        // Invocar método
        instancia.eliminarDiploma(idDiploma);

        // Validar resultado
        verify(diplomaEmitidoRepository).deleteAll(anyList());
        verify(diplomaRepository).delete(diploma);
    }

    @Test
    void emitirDiplomasFalloCorreoMarcaErrorTest() {
        // Preparar datos
        doThrow(new RuntimeException("SMTP no disponible")).when(mailSender).send(any(MimeMessage.class));
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));
        when(asistenciaRepository.findByIdEvento(idEvento)).thenReturn(List.of(asistencia));
        when(diplomaEmitidoRepository.findByIdDiploma(idDiploma)).thenReturn(Collections.emptyList());
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(diplomaEmitidoRepository.save(any(DiplomaEmitido.class))).thenAnswer(inv -> inv.getArgument(0));

        // Invocar método
        longResultado = (Long) instancia.emitirDiplomas(idDiploma).get("totalErrores");

        // Validar resultado (se registró intento con estado ERROR)
        assertEquals(1L, longResultado);
    }

    @Test
    void crearDiplomaFirmaVaciaUsaValorPorDefectoTest() {
        // Preparar datos
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));
        when(diplomaRepository.findByIdEvento(idEvento)).thenReturn(Optional.empty());
        when(diplomaRepository.save(any(Diploma.class))).thenAnswer(inv -> {
            Diploma d = inv.getArgument(0);
            assertEquals("Administrador", d.getFirma());
            assertEquals("Personalizado", d.getDiseno());
            guardado = true;
            return d;
        });

        // Invocar método
        instancia.crearDiploma(idEvento, "   ", "   ", plantillaPdfBase64, firmaPngBase64);

        // Validar resultado
        assertTrue(guardado);
    }

    @Test
    void validacionFormatoConstanciaRechazoSinPdfTest() {
        // Preparar datos: plantilla vacía no genera PDF válido en emitir
        diploma.setPlantillaPdf("   ");
        when(diplomaRepository.findById(idDiploma)).thenReturn(Optional.of(diploma));

        // Invocar método
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> instancia.emitirDiplomas(idDiploma));

        // Validar resultado
        assertFalse(ex.getMessage().isEmpty());
    }
}
