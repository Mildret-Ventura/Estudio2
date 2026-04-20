package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.models.Alumno;
import com.eventnode.eventnodeapi.models.Asistencia;
import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.models.PreCheckin;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.AlumnoRepository;
import com.eventnode.eventnodeapi.repositories.AsistenciaRepository;
import com.eventnode.eventnodeapi.repositories.EventoRepository;
import com.eventnode.eventnodeapi.repositories.PreCheckinRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AsistenciaServiceTest {

    private static AsistenciaService instancia;

    private AsistenciaRepository asistenciaRepository;
    private PreCheckinRepository preCheckinRepository;
    private EventoRepository eventoRepository;
    private UsuarioRepository usuarioRepository;
    private AlumnoRepository alumnoRepository;

    private Integer idUsuario;
    private Integer idEvento;
    private String metodo;

    private PreCheckin preCheckin;
    private Evento evento;
    private Usuario usuario;
    private Alumno alumno;

    private boolean guardado;
    private List<Map<String, Object>> listaResultado;
    private long conteoResultado;
    private String mensajeEsperado;

    @BeforeAll
    public static void iniciarInstancia() {
        instancia = null;
    }

    @BeforeEach
    void resetearVariables() {
        idUsuario = 1;
        idEvento = 10;
        metodo = "QR";
        guardado = false;
        listaResultado = null;
        conteoResultado = 0;
        mensajeEsperado = null;

        asistenciaRepository = mock(AsistenciaRepository.class);
        preCheckinRepository = mock(PreCheckinRepository.class);
        eventoRepository = mock(EventoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        alumnoRepository = mock(AlumnoRepository.class);

        instancia = new AsistenciaService(
                asistenciaRepository,
                preCheckinRepository,
                eventoRepository,
                usuarioRepository,
                alumnoRepository
        );

        preCheckin = new PreCheckin();
        preCheckin.setIdUsuario(idUsuario);
        preCheckin.setIdEvento(idEvento);
        preCheckin.setEstado("ACTIVO");

        evento = new Evento();
        evento.setIdEvento(idEvento);
        evento.setEstado("ACTIVO");
        evento.setFechaInicio(LocalDateTime.now().minusMinutes(1));
        evento.setTiempoToleranciaMinutos(30);

        usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre("Ana");
        usuario.setApellidoPaterno("Gomez");
        usuario.setApellidoMaterno("Lopez");
        usuario.setCorreo("ana@test.com");

        alumno = new Alumno();
        alumno.setIdUsuario(idUsuario);
        alumno.setCuatrimestre(4);
    }

    @Test
    void registroExitosoAsistenciaEventoActivoTest() {
        // Preparar datos
        when(preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.of(preCheckin));
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));
        when(asistenciaRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.empty());
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> {
            guardado = true;
            return inv.getArgument(0);
        });

        // Invocar método
        instancia.registrarAsistencia(idUsuario, idEvento, metodo);

        // Validar resultado
        assertTrue(guardado);
    }

    @Test
    void rechazoAsistenciaFueraVentanaPermitidaTest() {
        // Preparar datos (equivalente práctico a “evento ya pasó” respecto a la ventana de check-in)
        evento.setFechaInicio(LocalDateTime.now().minusHours(2));
        evento.setTiempoToleranciaMinutos(15);
        mensajeEsperado = "No estás dentro del tiempo permitido para registrar asistencia";

        when(preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.of(preCheckin));
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));
        when(asistenciaRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.empty());

        // Invocar método
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> instancia.registrarAsistencia(idUsuario, idEvento, metodo));

        // Validar resultado
        assertEquals(mensajeEsperado, ex.getMessage());
        assertFalse(guardado);
    }

    @Test
    void validacionDuplicidadAsistenciaTest() {
        // Preparar datos
        mensajeEsperado = "El usuario ya ha registrado asistencia";
        Asistencia yaExiste = new Asistencia();
        yaExiste.setIdAsistencia(99);

        when(preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.of(preCheckin));
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));
        when(asistenciaRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.of(yaExiste));

        // Invocar método
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> instancia.registrarAsistencia(idUsuario, idEvento, metodo));

        // Validar resultado
        assertEquals(mensajeEsperado, ex.getMessage());
    }

    @Test
    void controlToleranciaMinutosDentroDeLaVentanaTest() {
        // Preparar datos: ventana amplia alrededor de fechaInicio para evitar fallos por milisegundos
        evento.setFechaInicio(LocalDateTime.now().minusMinutes(5));
        evento.setTiempoToleranciaMinutos(60);

        when(preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.of(preCheckin));
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));
        when(asistenciaRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.empty());
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> {
            guardado = true;
            return inv.getArgument(0);
        });

        // Invocar método
        instancia.registrarAsistencia(idUsuario, idEvento, metodo);

        // Validar resultado
        assertTrue(guardado);
    }

    @Test
    void rechazoUsuarioNoInscritoTest() {
        // Preparar datos (caso nulo / sin pre-check-in)
        when(preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.empty());

        // Invocar método
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> instancia.registrarAsistencia(idUsuario, idEvento, metodo));

        // Validar resultado
        assertTrue(ex.getMessage().contains("no está inscrito"));
    }

    @Test
    void rechazoInscripcionNoActivaTest() {
        // Preparar datos
        preCheckin.setEstado("CANCELADO");
        when(preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.of(preCheckin));

        // Invocar método
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> instancia.registrarAsistencia(idUsuario, idEvento, metodo));

        // Validar resultado
        assertEquals("La inscripción no está activa", ex.getMessage());
    }

    @Test
    void rechazoEventoNoActivoTest() {
        // Preparar datos
        evento.setEstado("FINALIZADO");
        when(preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.of(preCheckin));
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));

        // Invocar método
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> instancia.registrarAsistencia(idUsuario, idEvento, metodo));

        // Validar resultado
        assertEquals("El evento no está activo", ex.getMessage());
    }

    @Test
    void registroAsistenciaManualExitosoTest() {
        // Preparar datos
        String matricula = "A09999999";
        when(alumnoRepository.findByMatricula(matricula)).thenReturn(Optional.of(alumno));
        when(preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.of(preCheckin));
        when(eventoRepository.findById(idEvento)).thenReturn(Optional.of(evento));
        when(asistenciaRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)).thenReturn(Optional.empty());
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> {
            guardado = true;
            return inv.getArgument(0);
        });

        // Invocar método
        instancia.registrarAsistenciaManual(matricula, idEvento);

        // Validar resultado
        assertTrue(guardado);
    }

    @Test
    void listarAsistenciasVaciasTest() {
        // Preparar datos
        when(asistenciaRepository.findByIdEvento(idEvento)).thenReturn(Collections.emptyList());

        // Invocar método
        listaResultado = instancia.listarAsistencias(idEvento);

        // Validar resultado
        assertNotNull(listaResultado);
        assertTrue(listaResultado.isEmpty());
    }

    @Test
    void listarAsistenciasConDatosTest() {
        // Preparar datos
        Asistencia a = new Asistencia();
        a.setIdAsistencia(1);
        a.setIdUsuario(idUsuario);
        a.setIdEvento(idEvento);
        a.setMetodo("MANUAL");
        a.setEstado("PENDIENTE");
        a.setFechaCheckin(LocalDateTime.now());

        when(asistenciaRepository.findByIdEvento(idEvento)).thenReturn(List.of(a));
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(alumnoRepository.findById(idUsuario)).thenReturn(Optional.of(alumno));

        // Invocar método
        listaResultado = instancia.listarAsistencias(idEvento);

        // Validar resultado
        assertEquals(1, listaResultado.size());
        assertEquals("MANUAL", listaResultado.get(0).get("metodo"));
        assertEquals(4, listaResultado.get(0).get("cuatrimestre"));
    }

    @Test
    void actualizarEstadoValidoTest() {
        // Preparar datos
        Asistencia a = new Asistencia();
        a.setIdAsistencia(5);
        a.setEstado("PENDIENTE");
        when(asistenciaRepository.findById(5)).thenReturn(Optional.of(a));
        when(asistenciaRepository.save(any(Asistencia.class))).thenReturn(a);

        // Invocar método
        instancia.actualizarEstado(5, "ASISTIDO");

        // Validar resultado
        assertEquals("ASISTIDO", a.getEstado());
    }

    @Test
    void actualizarEstadoInvalidoTest() {
        // Preparar datos
        Asistencia a = new Asistencia();
        a.setIdAsistencia(5);
        when(asistenciaRepository.findById(5)).thenReturn(Optional.of(a));

        // Invocar método
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> instancia.actualizarEstado(5, "OTRO"));

        // Validar resultado
        assertTrue(ex.getMessage().contains("Estado inválido"));
    }

    @Test
    void listarAsistenciasSinUsuarioRelacionadoTest() {
        // Preparar datos (usuario no encontrado en repositorio)
        Asistencia a = new Asistencia();
        a.setIdAsistencia(2);
        a.setIdUsuario(999);
        a.setIdEvento(idEvento);
        a.setMetodo("QR");
        a.setEstado("PENDIENTE");

        when(asistenciaRepository.findByIdEvento(idEvento)).thenReturn(List.of(a));
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // Invocar método
        listaResultado = instancia.listarAsistencias(idEvento);

        // Validar resultado
        assertEquals(1, listaResultado.size());
        assertFalse(listaResultado.get(0).containsKey("cuatrimestre"));
    }

    @Test
    void contarAsistenciasTest() {
        // Preparar datos
        when(asistenciaRepository.countByIdEvento(idEvento)).thenReturn(7L);

        // Invocar método
        conteoResultado = instancia.contarAsistencias(idEvento);

        // Validar resultado
        assertEquals(7L, conteoResultado);
    }
}
