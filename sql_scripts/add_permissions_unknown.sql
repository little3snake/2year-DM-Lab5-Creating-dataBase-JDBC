CREATE OR REPLACE FUNCTION assign_permissions_unknown(
    db_name VARCHAR,
    role VARCHAR,
    privileges_string TEXT  -- Привилегии в виде строки, например "SELECT;INSERT;UPDATE"
)
RETURNS VOID AS
$$
DECLARE
privilege TEXT;
BEGIN
    -- Разбиваем строку привилегий на отдельные слова
    FOR privilege IN SELECT unnest(string_to_array(privileges_string, ';'))
    LOOP
        -- Назначаем соответствующую привилегию
        IF privilege = 'SELECT' THEN
            EXECUTE format('GRANT SELECT ON TABLE public.cruises TO %I', role);
        ELSIF privilege = 'INSERT' THEN
            EXECUTE format('GRANT INSERT ON TABLE public.cruises TO %I', role);
        ELSIF privilege = 'UPDATE' THEN
            EXECUTE format('GRANT UPDATE ON TABLE public.cruises TO %I', role);
        ELSIF privilege = 'DELETE' THEN
            EXECUTE format('GRANT DELETE ON TABLE public.cruises TO %I', role);
        ELSE
            RAISE NOTICE 'Неизвестная привилегия: %', privilege;
        END IF;
    END LOOP;

    EXECUTE format('GRANT USAGE, SELECT ON SEQUENCE cruises_id_seq TO %I', role);
    -- Также выдаем доступ к последовательности, если даны привилегии на изменение данных
    --IF privileges_string ~ 'INSERT|UPDATE|DELETE' THEN
        --EXECUTE format('GRANT USAGE, SELECT ON SEQUENCE cruises_id_seq TO %I', role);
    --END IF;
END;
$$ LANGUAGE plpgsql;
