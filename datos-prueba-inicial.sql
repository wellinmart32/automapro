-- ============================================
-- SCRIPT DE DATOS DE PRUEBA - AutomaPro
-- APLICACIONES REALES + Licencia MASTER
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
-- USUARIOS
-- ============================================

INSERT INTO usuarios (nombre, email, password, rol, activo, fecha_creacion) VALUES 
('Admin AutomaPro', 'admin@automapro.com', '$2a$10$jlLDFL5vc8N7CvYB4FvlQuceAc1LOa2S1olrB08ByrJYrv1ka1U.G', 'ROLE_ADMIN', true, NOW()),
('Juan Pérez', 'juan@gmail.com', '$2a$10$RvbWwMh.3fuZrQFEG33Q7Onj9i8fL78v.h4d76eFA.73TDGcGVEEi', 'ROLE_CLIENTE', true, NOW()),
('María González', 'maria@gmail.com', '$2a$10$FKUrcigy0wee0Pvr/90rx.zVQcOtJWG6jLPx8ipsoLIZWk8u0z1IG', 'ROLE_CLIENTE', true, NOW()),
('Wellington Mero', 'welli@automapro.com', '$2a$10$jlLDFL5vc8N7CvYB4FvlQuceAc1LOa2S1olrB08ByrJYrv1ka1U.G', 'ROLE_ADMIN', true, NOW());

-- Contraseñas:
-- admin@automapro.com: admin123
-- juan@gmail.com: juan123
-- maria@gmail.com: maria123
-- welli@automapro.com: admin123

-- ============================================
-- APLICACIONES REALES
-- ============================================

INSERT INTO aplicaciones (nombre, descripcion, version, imagen_url, precio, dias_trial, activo, fecha_creacion, ruta_archivo) 
VALUES 
    (
        'Automatización de Mensajes Bíblicos',
        'Aplicación de escritorio que automatiza el envío de mensajes bíblicos diarios. Permite programar mensajes, gestionar contactos y personalizar textos inspiradores.',
        '1.0.0',
        'https://via.placeholder.com/400x300/0d6efd/ffffff?text=Mensajes+Biblicos',
        29.99,
        15,
        true,
        NOW(),
        NULL
    ),
    (
        'Publicador Automático Marketplace',
        'Herramienta para automatizar la publicación de productos en múltiples marketplaces. Gestión centralizada de inventario, precios y descripciones.',
        '1.0.0',
        'https://via.placeholder.com/400x300/198754/ffffff?text=Auto+Marketplace',
        49.99,
        30,
        true,
        NOW(),
        NULL
    );

-- ============================================
-- LICENCIAS DE PRUEBA
-- ============================================

-- Licencia MASTER para Wellington (funciona en TODAS las apps)
INSERT INTO licencias (usuario_id, aplicacion_id, codigo, tipo_licencia, fecha_inicio_uso, dias_trial, fecha_expiracion, activo, fecha_creacion)
VALUES (4, NULL, 'LIC-MASTER-WELLI', 'FULL', NOW(), NULL, NULL, true, NOW());

-- Licencia TRIAL para Juan (App 1)
INSERT INTO licencias (usuario_id, aplicacion_id, codigo, tipo_licencia, fecha_inicio_uso, dias_trial, fecha_expiracion, activo, fecha_creacion)
VALUES (2, 1, 'LIC-TRIAL001', 'TRIAL', NOW(), 15, NOW() + INTERVAL '15 days', true, NOW());

-- Licencia FULL para María (App 1)
INSERT INTO licencias (usuario_id, aplicacion_id, codigo, tipo_licencia, fecha_inicio_uso, dias_trial, fecha_expiracion, activo, fecha_creacion)
VALUES (3, 1, 'LIC-FULL001', 'FULL', NOW(), NULL, NULL, true, NOW());

-- Licencia TRIAL para Juan (App 2)
INSERT INTO licencias (usuario_id, aplicacion_id, codigo, tipo_licencia, fecha_inicio_uso, dias_trial, fecha_expiracion, activo, fecha_creacion)
VALUES (2, 2, 'LIC-TRIAL002', 'TRIAL', NOW(), 30, NOW() + INTERVAL '30 days', true, NOW());

-- Licencia FULL para María (App 2)
INSERT INTO licencias (usuario_id, aplicacion_id, codigo, tipo_licencia, fecha_inicio_uso, dias_trial, fecha_expiracion, activo, fecha_creacion)
VALUES (3, 2, 'LIC-FULL002', 'FULL', NOW(), NULL, NULL, true, NOW());

-- ============================================
-- VERIFICACIÓN
-- ============================================
SELECT '=== RESUMEN ===' as info;
SELECT 'Usuarios:' as tabla, COUNT(*) as total FROM usuarios;
SELECT 'Aplicaciones:' as tabla, COUNT(*) as total FROM aplicaciones;
SELECT 'Licencias:' as tabla, COUNT(*) as total FROM licencias;

-- Mostrar todas las licencias
SELECT 
    l.codigo as "Código",
    u.nombre as "Usuario",
    COALESCE(a.nombre, '*** TODAS LAS APPS ***') as "Aplicación",
    l.tipo_licencia as "Tipo",
    l.activo as "Activa"
FROM licencias l
LEFT JOIN usuarios u ON l.usuario_id = u.id
LEFT JOIN aplicaciones a ON l.aplicacion_id = a.id
ORDER BY l.id;