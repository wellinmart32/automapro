-- ============================================
-- SCRIPT DE DATOS DE PRUEBA - AutomaPro
-- CON HASHES REALES DEL SISTEMA
-- ============================================

-- Limpiar datos existentes
DELETE FROM descargas;
DELETE FROM licencias;
DELETE FROM aplicaciones;
DELETE FROM usuarios;

-- Reiniciar secuencias
ALTER SEQUENCE usuarios_id_seq RESTART WITH 1;
ALTER SEQUENCE aplicaciones_id_seq RESTART WITH 1;
ALTER SEQUENCE licencias_id_seq RESTART WITH 1;
ALTER SEQUENCE descargas_id_seq RESTART WITH 1;

-- ============================================
-- USUARIOS (con hashes generados por el sistema)
-- ============================================

INSERT INTO usuarios (nombre, email, password, rol, activo, fecha_creacion) VALUES 
('Admin AutomaPro', 'admin@automapro.com', '$2a$10$jlLDFL5vc8N7CvYB4FvlQuceAc1LOa2S1olrB08ByrJYrv1ka1U.G', 'ROLE_ADMIN', true, NOW()),
('Juan Pérez', 'juan@gmail.com', '$2a$10$RvbWwMh.3fuZrQFEG33Q7Onj9i8fL78v.h4d76eFA.73TDGcGVEEi', 'ROLE_CLIENTE', true, NOW()),
('María González', 'maria@gmail.com', '$2a$10$FKUrcigy0wee0Pvr/90rx.zVQcOtJWG6jLPx8ipsoLIZWk8u0z1IG', 'ROLE_CLIENTE', true, NOW()),
('Carlos Rodríguez', 'carlos@gmail.com', '$2a$10$qRc1o.ZqSbdOWly0Qduc8Op899x9n57Ded4SY7pEGCGus1ZEkQSzO', 'ROLE_CLIENTE', true, NOW());

-- ============================================
-- APLICACIONES
-- ============================================

INSERT INTO aplicaciones (nombre, descripcion, version, imagen_url, precio, dias_trial, activo, fecha_creacion) 
VALUES 
    (
        'Editor de Imágenes Pro',
        'Potente editor de imágenes con herramientas profesionales para retoque fotográfico, diseño gráfico y edición avanzada. Incluye capas, máscaras, efectos y filtros.',
        '2.5.1',
        'https://via.placeholder.com/400x300/0d6efd/ffffff?text=Editor+Pro',
        49.99,
        30,
        true,
        NOW()
    ),
    (
        'Gestor de Contraseñas',
        'Administrador seguro de contraseñas con cifrado AES-256. Genera contraseñas fuertes, sincroniza entre dispositivos y autocompletado inteligente.',
        '1.8.3',
        'https://via.placeholder.com/400x300/198754/ffffff?text=Password+Manager',
        29.99,
        14,
        true,
        NOW()
    ),
    (
        'Suite Ofimática Premium',
        'Suite completa de oficina con procesador de textos, hojas de cálculo y presentaciones. Compatible con formatos Microsoft Office.',
        '5.2.0',
        'https://via.placeholder.com/400x300/dc3545/ffffff?text=Office+Suite',
        79.99,
        30,
        true,
        NOW()
    ),
    (
        'Antivirus Total Security',
        'Protección completa contra virus, malware, ransomware y amenazas online. Firewall integrado y VPN incluida.',
        '3.1.4',
        'https://via.placeholder.com/400x300/ffc107/000000?text=Antivirus',
        39.99,
        30,
        true,
        NOW()
    ),
    (
        'Herramienta de Backup',
        'Copia de seguridad automática en la nube y local. Programación flexible, cifrado y restauración rápida.',
        '4.0.2',
        'https://via.placeholder.com/400x300/0dcaf0/000000?text=Backup+Tool',
        0.00,
        60,
        true,
        NOW()
    );

-- ============================================
-- LICENCIAS
-- ============================================

-- Juan (ID=2): 1 TRIAL vigente, 1 TRIAL expirada
INSERT INTO licencias (usuario_id, aplicacion_id, codigo, tipo_licencia, fecha_inicio_uso, dias_trial, fecha_expiracion, activo, fecha_creacion) 
VALUES 
    (2, 1, 'LIC-JUAN001', 'TRIAL', NOW() - INTERVAL '5 days', 30, NOW() + INTERVAL '25 days', true, NOW() - INTERVAL '5 days'),
    (2, 2, 'LIC-JUAN002', 'TRIAL', NOW() - INTERVAL '20 days', 14, NOW() - INTERVAL '6 days', true, NOW() - INTERVAL '20 days');

-- María (ID=3): 2 FULL permanentes
INSERT INTO licencias (usuario_id, aplicacion_id, codigo, tipo_licencia, activo, fecha_creacion) 
VALUES 
    (3, 3, 'LIC-MARIA001', 'FULL', true, NOW()),
    (3, 5, 'LIC-MARIA002', 'FULL', true, NOW());

-- Carlos (ID=4): 2 TRIAL vigentes
INSERT INTO licencias (usuario_id, aplicacion_id, codigo, tipo_licencia, fecha_inicio_uso, dias_trial, fecha_expiracion, activo, fecha_creacion) 
VALUES 
    (4, 1, 'LIC-CARLOS001', 'TRIAL', NOW() - INTERVAL '2 days', 30, NOW() + INTERVAL '28 days', true, NOW() - INTERVAL '2 days'),
    (4, 4, 'LIC-CARLOS002', 'TRIAL', NOW() - INTERVAL '1 day', 30, NOW() + INTERVAL '29 days', true, NOW() - INTERVAL '1 day');

-- ============================================
-- CREDENCIALES DE PRUEBA
-- ============================================
-- ADMIN: admin@automapro.com / Admin123
-- Juan: juan@gmail.com / 123456
-- María: maria@gmail.com / 123456
-- Carlos: carlos@gmail.com / 123456
-- ============================================

SELECT 'Datos completos insertados con hashes reales del sistema' AS resultado;