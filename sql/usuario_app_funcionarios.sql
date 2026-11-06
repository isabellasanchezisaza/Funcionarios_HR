-- Ejecutar con el usuario postgres, DESPUÉS de crear las tablas.
-- Cambie CAMBIAR_CLAVE por su propia clave antes de ejecutar.
CREATE USER app_funcionarios WITH PASSWORD 'CAMBIAR_CLAVE';
GRANT CONNECT ON DATABASE db_funcionarios TO app_funcionarios;
GRANT USAGE ON SCHEMA public TO app_funcionarios;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_funcionarios;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_funcionarios;

-- Mínimo privilegio: la app solo lee los roles y no borra usuarios
REVOKE INSERT, UPDATE, DELETE ON roles FROM app_funcionarios;
REVOKE DELETE ON usuarios FROM app_funcionarios;