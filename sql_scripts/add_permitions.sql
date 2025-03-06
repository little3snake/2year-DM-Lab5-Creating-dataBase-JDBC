CREATE OR REPLACE FUNCTION assign_permissions(
    db_name VARCHAR,  -- имя базы данных
    --username VARCHAR,  -- имя пользователя
    role VARCHAR  -- роль пользователя
)
RETURNS VOID AS
$$
BEGIN
    -- Назначаем права в зависимости от роли пользователя
    IF role = 'admin' THEN
        -- Для администратора даем полный доступ
        EXECUTE format('GRANT ALL PRIVILEGES ON TABLE public.cruises TO %I', role);--db_name, username); %I insteadof public
        EXECUTE format ('GRANT USAGE, SELECT ON SEQUENCE cruises_id_seq TO %I', role);
    ELSIF role = 'guest' THEN
        -- Для гостя даем только права на чтение и поиск
        EXECUTE format('GRANT CONNECT ON DATABASE cruise_db TO %I', role);
        EXECUTE format('GRANT USAGE ON SCHEMA public TO %I', role);
        EXECUTE format('GRANT SELECT ON TABLE public.cruises TO %I', role);--db_name, username);
        EXECUTE format ('GRANT USAGE, SELECT ON SEQUENCE cruises_id_seq TO %I', role);
ELSE
        -- Если роль не указана, можем дать ограниченные права (например, права на чтение)
        EXECUTE format('GRANT USAGE ON SCHEMA public TO %I', role);
        EXECUTE format('GRANT SELECT ON TABLE public.cruises TO %I', role);--db_name, username);
END IF;

    -- Применяем изменения
    -- В PostgreSQL нет необходимости в `FLUSH PRIVILEGES`, так как права применяются сразу
END;
$$ LANGUAGE plpgsql;
