-- v8.1: MigracionBackendConfig ejecuta este script completo dentro de una transaccion.
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS proveedor_externo varchar(20);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS id_externo varchar(255);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_tarjeta_ultimos4 varchar(4);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_yape_celular varchar(9);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_plin_celular varchar(9);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_transferencia_cuenta varchar(20);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_efectivo boolean NOT NULL DEFAULT false;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_tarjeta_id bigint;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_yape_id bigint;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_plin_id bigint;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_transferencia_id bigint;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pago_efectivo_id bigint;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS materiales varchar(255);
ALTER TABLE publicaciones_material ADD COLUMN IF NOT EXISTS franja_horaria varchar(20);
ALTER TABLE publicaciones_material ADD COLUMN IF NOT EXISTS monto_pago numeric(10,2);
ALTER TABLE publicaciones_material ADD COLUMN IF NOT EXISTS metodo_pago varchar(20);
ALTER TABLE publicaciones_material ADD COLUMN IF NOT EXISTS motivo_cancelacion varchar(500);
ALTER TABLE solicitudes_recoleccion ADD COLUMN IF NOT EXISTS franja_horaria varchar(20);
ALTER TABLE solicitudes_recoleccion ADD COLUMN IF NOT EXISTS comentario_calificacion varchar(200);
ALTER TABLE puntos_reciclaje ADD COLUMN IF NOT EXISTS activo boolean NOT NULL DEFAULT true;

DO $$
DECLARE repetidos bigint;
BEGIN
    IF to_regclass('public.identidades_externas') IS NOT NULL THEN
        SELECT count(*) INTO repetidos FROM (
            SELECT usuario_id FROM identidades_externas GROUP BY usuario_id HAVING count(*) > 1
        ) AS usuarios_repetidos;
        IF repetidos > 0 THEN
            RAISE WARNING 'v8.1: % usuarios con varias identidades; se conserva la de menor id', repetidos;
        END IF;
        UPDATE usuarios u SET proveedor_externo = i.proveedor, id_externo = i.identificador
        FROM (SELECT DISTINCT ON (usuario_id) * FROM identidades_externas ORDER BY usuario_id, id) i
        WHERE u.id = i.usuario_id AND u.proveedor_externo IS NULL;
    END IF;

    IF to_regclass('public.metodos_pago_usuario') IS NOT NULL THEN
        -- Detenerse antes de perder datos que no caben en una sola columna por tipo.
        IF EXISTS (SELECT 1 FROM metodos_pago_usuario GROUP BY usuario_id, tipo HAVING count(*) > 1) THEN
            RAISE EXCEPTION 'v8.1: existen varios metodos del mismo tipo; resolver duplicados antes de migrar';
        END IF;
        UPDATE usuarios u SET pago_tarjeta_ultimos4 = m.dato, pago_tarjeta_id = m.id
            FROM metodos_pago_usuario m WHERE m.usuario_id = u.id AND m.tipo = 'tarjeta';
        UPDATE usuarios u SET pago_yape_celular = m.dato, pago_yape_id = m.id
            FROM metodos_pago_usuario m WHERE m.usuario_id = u.id AND m.tipo = 'yape';
        UPDATE usuarios u SET pago_plin_celular = m.dato, pago_plin_id = m.id
            FROM metodos_pago_usuario m WHERE m.usuario_id = u.id AND m.tipo = 'plin';
        UPDATE usuarios u SET pago_transferencia_cuenta = m.dato, pago_transferencia_id = m.id
            FROM metodos_pago_usuario m WHERE m.usuario_id = u.id AND m.tipo = 'transferencia';
        UPDATE usuarios u SET pago_efectivo = true, pago_efectivo_id = m.id
            FROM metodos_pago_usuario m WHERE m.usuario_id = u.id AND m.tipo = 'efectivo';
        UPDATE usuarios u SET metodo_pago_preferido = m.tipo
            FROM metodos_pago_usuario m WHERE m.usuario_id = u.id AND m.predeterminado;
    END IF;

    IF to_regclass('public.usuarios_materiales') IS NOT NULL THEN
        UPDATE usuarios u SET materiales = m.ids FROM (
            SELECT usuario_id, string_agg(categoria_material_id::text, ',' ORDER BY categoria_material_id) AS ids
            FROM usuarios_materiales GROUP BY usuario_id
        ) m WHERE u.id = m.usuario_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'usuarios'::regclass
            AND conname = 'uk_usuario_identidad_externa') THEN
        ALTER TABLE usuarios ADD CONSTRAINT uk_usuario_identidad_externa UNIQUE (proveedor_externo, id_externo);
    END IF;
END $$;

-- Solo se retiran las tablas del cierre anterior despues de copiar sus datos.
DROP TABLE IF EXISTS identidades_externas;
DROP TABLE IF EXISTS metodos_pago_usuario;
DROP TABLE IF EXISTS usuarios_materiales;

-- Preferencias historicas incompletas se conservan sin inventar numeros.
UPDATE usuarios SET pago_efectivo = true WHERE metodo_pago_preferido = 'efectivo';

UPDATE usuarios SET foto_perfil_url = regexp_replace(foto_perfil_url, '^uploads/fotos_perfil/', '/api/v1/files/fotos_perfil--')
 WHERE foto_perfil_url LIKE 'uploads/fotos_perfil/%';
UPDATE publicaciones_material SET foto_url = regexp_replace(foto_url, '^uploads/fotos_publicaciones/', '/api/v1/files/fotos_publicaciones--')
 WHERE foto_url LIKE 'uploads/fotos_publicaciones/%';
UPDATE documentos_verificacion SET url_archivo = regexp_replace(url_archivo, '^uploads/documentos_verificacion/', '/api/v1/files/documentos_verificacion--')
 WHERE url_archivo LIKE 'uploads/documentos_verificacion/%';
UPDATE tickets_soporte SET evidencia_url = regexp_replace(evidencia_url, '^uploads/evidencias_tickets/', '/api/v1/files/evidencias_tickets--')
 WHERE evidencia_url LIKE 'uploads/evidencias_tickets/%';

-- Java completa los hashes nulos con BCrypt y ejecuta ALTER ... SET NOT NULL al finalizar.
