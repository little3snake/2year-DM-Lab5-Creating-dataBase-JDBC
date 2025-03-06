CREATE OR REPLACE FUNCTION check_privilege_on_table(user_role TEXT, operation TEXT, table_name TEXT)
RETURNS BOOLEAN AS
$$
BEGIN
    -- Выполняем проверку прав пользователя на выполнение операции на таблице
    IF operation = 'SELECT' THEN
        RETURN has_table_privilege(user_role, table_name, 'SELECT');
    ELSIF operation = 'INSERT' THEN
        RETURN has_table_privilege(user_role, table_name, 'INSERT');
    ELSIF operation = 'UPDATE' THEN
        RETURN has_table_privilege(user_role, table_name, 'UPDATE');
    ELSIF operation = 'DELETE' THEN
        RETURN has_table_privilege(user_role, table_name, 'DELETE');
ELSE
        RETURN FALSE;  -- Если операция не поддерживается, возвращаем FALSE
END IF;
END;
$$ LANGUAGE plpgsql;
