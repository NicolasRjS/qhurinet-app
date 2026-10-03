-- ============ DATOS DEMO: reemplazar por datos reales antes de la entrega ============
INSERT INTO puntos_reciclaje (nombre, tipo, direccion, distrito, latitud, longitud, horario_atencion, calificacion_promedio)
SELECT 'Punto Verde Miraflores 01', 'acopio', 'Av. José Larco 820', 'Miraflores', -12.1218, -77.0296, 'Lunes a sábado de 8:00 a 18:00', 0
WHERE NOT EXISTS (SELECT 1 FROM puntos_reciclaje WHERE nombre = 'Punto Verde Miraflores 01');

INSERT INTO puntos_reciclaje (nombre, tipo, direccion, distrito, latitud, longitud, horario_atencion, calificacion_promedio)
SELECT 'Punto Verde San Isidro 01', 'bodega', 'Av. Arequipa 3150', 'San Isidro', -12.0974, -77.0367, 'Lunes a viernes de 9:00 a 18:00', 0
WHERE NOT EXISTS (SELECT 1 FROM puntos_reciclaje WHERE nombre = 'Punto Verde San Isidro 01');

INSERT INTO puntos_reciclaje (nombre, tipo, direccion, distrito, latitud, longitud, horario_atencion, calificacion_promedio)
SELECT 'Punto Verde Santiago de Surco 01', 'reciclador', 'Av. Caminos del Inca 1450', 'Surco', -12.1322, -76.9967, 'Lunes a sábado de 8:30 a 17:30', 0
WHERE NOT EXISTS (SELECT 1 FROM puntos_reciclaje WHERE nombre = 'Punto Verde Santiago de Surco 01');

INSERT INTO puntos_reciclaje (nombre, tipo, direccion, distrito, latitud, longitud, horario_atencion, calificacion_promedio)
SELECT 'Punto Verde San Borja 01', 'municipal', 'Av. San Luis 2050', 'San Borja', -12.1010, -76.9930, 'Lunes a domingo de 8:00 a 17:00', 0
WHERE NOT EXISTS (SELECT 1 FROM puntos_reciclaje WHERE nombre = 'Punto Verde San Borja 01');

INSERT INTO puntos_reciclaje (nombre, tipo, direccion, distrito, latitud, longitud, horario_atencion, calificacion_promedio)
SELECT 'Punto Verde Barranco 01', 'acopio', 'Av. Grau 620', 'Barranco', -12.1456, -77.0204, 'Lunes a sábado de 9:00 a 18:00', 0
WHERE NOT EXISTS (SELECT 1 FROM puntos_reciclaje WHERE nombre = 'Punto Verde Barranco 01');

INSERT INTO puntos_reciclaje (nombre, tipo, direccion, distrito, latitud, longitud, horario_atencion, calificacion_promedio)
SELECT 'Punto Verde Jesús María 01', 'bodega', 'Av. Brasil 1250', 'Jesús María', -12.0774, -77.0472, 'Lunes a viernes de 8:00 a 18:00', 0
WHERE NOT EXISTS (SELECT 1 FROM puntos_reciclaje WHERE nombre = 'Punto Verde Jesús María 01');

INSERT INTO puntos_reciclaje (nombre, tipo, direccion, distrito, latitud, longitud, horario_atencion, calificacion_promedio)
SELECT 'Punto Verde Lince 01', 'reciclador', 'Av. Arequipa 1850', 'Lince', -12.0841, -77.0361, 'Lunes a sábado de 8:00 a 17:00', 0
WHERE NOT EXISTS (SELECT 1 FROM puntos_reciclaje WHERE nombre = 'Punto Verde Lince 01');

INSERT INTO puntos_reciclaje (nombre, tipo, direccion, distrito, latitud, longitud, horario_atencion, calificacion_promedio)
SELECT 'Punto Verde Surquillo 01', 'municipal', 'Av. Angamos Este 1050', 'Surquillo', -12.1135, -77.0186, 'Lunes a domingo de 8:30 a 17:30', 0
WHERE NOT EXISTS (SELECT 1 FROM puntos_reciclaje WHERE nombre = 'Punto Verde Surquillo 01');

INSERT INTO punto_reciclaje_materiales (punto_reciclaje_id, categoria_material_id)
SELECT p.id, c.id FROM puntos_reciclaje p, categorias_material c
WHERE p.nombre = 'Punto Verde Miraflores 01' AND c.nombre IN ('cartón', 'PET', 'vidrio')
ON CONFLICT DO NOTHING;

INSERT INTO punto_reciclaje_materiales (punto_reciclaje_id, categoria_material_id)
SELECT p.id, c.id FROM puntos_reciclaje p, categorias_material c
WHERE p.nombre = 'Punto Verde San Isidro 01' AND c.nombre IN ('PET', 'metal')
ON CONFLICT DO NOTHING;

INSERT INTO punto_reciclaje_materiales (punto_reciclaje_id, categoria_material_id)
SELECT p.id, c.id FROM puntos_reciclaje p, categorias_material c
WHERE p.nombre = 'Punto Verde Santiago de Surco 01' AND c.nombre IN ('cartón', 'plástico', 'vidrio')
ON CONFLICT DO NOTHING;

INSERT INTO punto_reciclaje_materiales (punto_reciclaje_id, categoria_material_id)
SELECT p.id, c.id FROM puntos_reciclaje p, categorias_material c
WHERE p.nombre = 'Punto Verde San Borja 01' AND c.nombre IN ('metal', 'vidrio', 'PET')
ON CONFLICT DO NOTHING;

INSERT INTO punto_reciclaje_materiales (punto_reciclaje_id, categoria_material_id)
SELECT p.id, c.id FROM puntos_reciclaje p, categorias_material c
WHERE p.nombre = 'Punto Verde Barranco 01' AND c.nombre IN ('vidrio', 'plástico')
ON CONFLICT DO NOTHING;

INSERT INTO punto_reciclaje_materiales (punto_reciclaje_id, categoria_material_id)
SELECT p.id, c.id FROM puntos_reciclaje p, categorias_material c
WHERE p.nombre = 'Punto Verde Jesús María 01' AND c.nombre IN ('cartón', 'metal')
ON CONFLICT DO NOTHING;

INSERT INTO punto_reciclaje_materiales (punto_reciclaje_id, categoria_material_id)
SELECT p.id, c.id FROM puntos_reciclaje p, categorias_material c
WHERE p.nombre = 'Punto Verde Lince 01' AND c.nombre IN ('PET', 'cartón', 'plástico')
ON CONFLICT DO NOTHING;

INSERT INTO punto_reciclaje_materiales (punto_reciclaje_id, categoria_material_id)
SELECT p.id, c.id FROM puntos_reciclaje p, categorias_material c
WHERE p.nombre = 'Punto Verde Surquillo 01' AND c.nombre IN ('metal', 'PET', 'vidrio')
ON CONFLICT DO NOTHING;

INSERT INTO usuarios (nombre_completo, email, password_hash, telefono, rol_id, estado,
                      en_linea, calificacion_promedio, fecha_registro)
SELECT 'Administrador DEMO', 'admin@qhurinet.demo',
       '$2a$10$n83Sjom.NrduAjavivWsseApDLkZFnOSv0PRentQTfnreqwLJ/Bcm',
       '999000001', r.id, 'activo', true, 0, now()
FROM roles r
WHERE r.nombre = 'administrador'
ON CONFLICT (email) DO NOTHING;
