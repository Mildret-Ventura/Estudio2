-- =====================================================
-- CREAR BASE DE DATOS
-- =====================================================
CREATE DATABASE IF NOT EXISTS event_node CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE event_node;
SET NAMES utf8mb4;

-- =====================================================
-- TABLA: ROLES
-- =====================================================
CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (nombre) VALUES
('ALUMNO'),
('ADMINISTRADOR'),
('SUPERADMIN');


-- =====================================================
-- TABLA: USUARIOS (TABLA PADRE)
-- =====================================================
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    
    recover_password VARCHAR(20),
    
    estado ENUM('ACTIVO','INACTIVO') DEFAULT 'ACTIVO',
    
    intentos_fallidos INT DEFAULT 0,
    bloqueado_hasta DATETIME NULL,
    
    rol_id INT NOT NULL,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (rol_id)
        REFERENCES roles(id_rol)
);


-- =====================================================
-- TABLA: ALUMNOS
-- =====================================================
CREATE TABLE alumnos (
    id_usuario INT PRIMARY KEY,
    
    matricula VARCHAR(20) NOT NULL UNIQUE,
    fecha_nac DATE NOT NULL,
    edad INT NOT NULL,
    sexo VARCHAR(20) NOT NULL,
    cuatrimestre INT NOT NULL,
    
    CONSTRAINT fk_alumno_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE,
        
    CONSTRAINT chk_edad CHECK (edad BETWEEN 17 AND 99),
    CONSTRAINT chk_cuatrimestre CHECK (cuatrimestre BETWEEN 1 AND 10)
);


-- =====================================================
-- TABLA: ADMINISTRADORES
-- =====================================================
CREATE TABLE administradores (
    id_usuario INT PRIMARY KEY,
    es_principal BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_admin_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE
);


-- =====================================================
-- TABLA: CATEGORIAS
-- =====================================================
CREATE TABLE categorias (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);


-- =====================================================
-- TABLA: EVENTOS
-- =====================================================
CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    ubicacion VARCHAR(200) NOT NULL,
    
    capacidad_maxima INT NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    
    tiempo_cancelacion_horas INT NOT NULL,
    tiempo_tolerancia_minutos INT DEFAULT 0,
    
    banner LONGTEXT,
    
    estado ENUM( 'PRÓXIMO','ACTIVO','CANCELADO','FINALIZADO') DEFAULT 'PRÓXIMO',
    
    id_categoria INT NOT NULL,
    creado_por INT NOT NULL,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_evento_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categorias(id_categoria),
        
    CONSTRAINT fk_evento_admin
        FOREIGN KEY (creado_por)
        REFERENCES usuarios(id_usuario),
        
    CONSTRAINT chk_capacidad CHECK (capacidad_maxima > 0)
);


-- =====================================================
-- TABLA: ORGANIZADORES
-- =====================================================
CREATE TABLE organizadores (
    id_organizador INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    correo VARCHAR(150)
);


-- =====================================================
-- TABLA: EVENTO_ORGANIZADOR
-- =====================================================
CREATE TABLE evento_organizador (
    id_evento INT NOT NULL,
    id_organizador INT NOT NULL,
    
    PRIMARY KEY (id_evento, id_organizador),
    
    CONSTRAINT fk_eo_evento
        FOREIGN KEY (id_evento)
        REFERENCES eventos(id_evento)
        ON DELETE CASCADE,
        
    CONSTRAINT fk_eo_organizador
        FOREIGN KEY (id_organizador)
        REFERENCES organizadores(id_organizador)
        ON DELETE CASCADE
);


-- =====================================================
-- TABLA: PRE_CHECKIN
-- =====================================================
CREATE TABLE pre_checkin (
    id_precheckin INT AUTO_INCREMENT PRIMARY KEY,
    
    id_usuario INT NOT NULL,
    id_evento INT NOT NULL,
    
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('ACTIVO','CANCELADO') DEFAULT 'ACTIVO',
    
    CONSTRAINT fk_precheck_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),
        
    CONSTRAINT fk_precheck_evento
        FOREIGN KEY (id_evento)
        REFERENCES eventos(id_evento),
        
    CONSTRAINT uq_usuario_evento
        UNIQUE (id_usuario, id_evento)
);


-- =====================================================
-- TABLA: ASISTENCIAS
-- =====================================================
CREATE TABLE asistencias (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    
    id_usuario INT NOT NULL,
    id_evento INT NOT NULL,
    
    fecha_checkin DATETIME DEFAULT CURRENT_TIMESTAMP,
    metodo ENUM('QR','MANUAL') NOT NULL,
    
    CONSTRAINT fk_asistencia_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),
        
    CONSTRAINT fk_asistencia_evento
        FOREIGN KEY (id_evento)
        REFERENCES eventos(id_evento),
        
    CONSTRAINT uq_asistencia
        UNIQUE (id_usuario, id_evento)
);


-- =====================================================
-- TABLA: DIPLOMAS
-- =====================================================
CREATE TABLE diplomas (
    id_diploma INT AUTO_INCREMENT PRIMARY KEY,
    
    id_evento INT NOT NULL UNIQUE,
    
    nombre_evento VARCHAR(200) NOT NULL,
    firma VARCHAR(255) NOT NULL,
    diseno VARCHAR(255) NOT NULL,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('ACTIVO','ELIMINADO') DEFAULT 'ACTIVO',
    
    CONSTRAINT fk_diploma_evento
        FOREIGN KEY (id_evento)
        REFERENCES eventos(id_evento)
        ON DELETE CASCADE
);


-- =====================================================
-- TABLA: DIPLOMAS_EMITIDOS
-- =====================================================
CREATE TABLE diplomas_emitidos (
    id_emitido INT AUTO_INCREMENT PRIMARY KEY,
    
    id_diploma INT NOT NULL,
    id_usuario INT NOT NULL,
    
    fecha_envio DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado_envio ENUM('ENVIADO','ERROR') DEFAULT 'ENVIADO',
    
    CONSTRAINT fk_emitido_diploma
        FOREIGN KEY (id_diploma)
        REFERENCES diplomas(id_diploma)
        ON DELETE CASCADE,
        
    CONSTRAINT fk_emitido_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE,
        
    CONSTRAINT uq_diploma_usuario
        UNIQUE (id_diploma, id_usuario)
);


-- =====================================================
-- ÍNDICES
-- =====================================================
CREATE INDEX idx_usuario_rol ON usuarios(rol_id);
CREATE INDEX idx_evento_fecha ON eventos(fecha_inicio);
CREATE INDEX idx_precheck_evento ON pre_checkin(id_evento);
CREATE INDEX idx_asistencia_evento ON asistencias(id_evento);
CREATE INDEX idx_eo_organizador ON evento_organizador(id_organizador);
