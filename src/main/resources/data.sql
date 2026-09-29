-- Datos iniciales de QhuriNet.
-- Se ejecuta en cada arranque despues de que Hibernate crea o actualiza las tablas
-- (spring.jpa.defer-datasource-initialization=true). ON CONFLICT DO NOTHING evita duplicados.
-- Las columnas con valor por defecto se escriben explicitamente: el DEFAULT vive en Java, no en la base.

-- Roles (excluyentes). El administrador no se ofrece en el registro publico.
INSERT INTO roles (nombre, descripcion) VALUES ('generador', 'Publica material reciclable') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (nombre, descripcion) VALUES ('recolector', 'Recoge material reciclable') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (nombre, descripcion) VALUES ('administrador', 'Cuenta creada manualmente por los desarrolladores') ON CONFLICT (nombre) DO NOTHING;

-- Categorias de material. Una publicacion corresponde a un unico material.
INSERT INTO categorias_material (nombre, unidad_medida_default) VALUES ('cartón', 'kg') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO categorias_material (nombre, unidad_medida_default) VALUES ('PET', 'kg') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO categorias_material (nombre, unidad_medida_default) VALUES ('vidrio', 'kg') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO categorias_material (nombre, unidad_medida_default) VALUES ('metal', 'kg') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO categorias_material (nombre, unidad_medida_default) VALUES ('plástico', 'kg') ON CONFLICT (nombre) DO NOTHING;
