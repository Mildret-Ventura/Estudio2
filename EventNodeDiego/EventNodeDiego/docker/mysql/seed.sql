-- =====================================================
-- SEED: DATOS INICIALES
-- =====================================================
USE event_node;
SET NAMES utf8mb4;

-- Insertar roles base (si no existen ya desde init.sql)
INSERT IGNORE INTO roles (nombre) VALUES ('ALUMNO'), ('ADMINISTRADOR'), ('SUPERADMIN');

-- Insertar categorías iniciales para eventos
INSERT IGNORE INTO categorias (nombre) VALUES
('Tecnología'),
('Deportes'),
('Cultura'),
('Académico'),
('Social');

-- Insertar Administrador Maestro (SuperAdmin)
-- Contraseña: Admin@1234 (almacenada como hash BCrypt)
INSERT INTO usuarios (nombre, apellido_paterno, apellido_materno, correo, password, estado, intentos_fallidos, rol_id, fecha_creacion)
VALUES ('Admin', 'EventNode', 'Principal', 'admin@eventnode.com', '$2b$12$ifqonBLg.LpMrdrNLDi/neai.oVibgSXnTaIFeTonbNv/ep4ajh32', 'ACTIVO', 0,
  (SELECT id_rol FROM roles WHERE nombre = 'SUPERADMIN'), NOW());

INSERT INTO administradores (id_usuario, es_principal)
VALUES (LAST_INSERT_ID(), TRUE);
