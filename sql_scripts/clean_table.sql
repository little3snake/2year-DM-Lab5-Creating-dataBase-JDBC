CREATE OR REPLACE FUNCTION clear_table(table_name VARCHAR)
RETURNS VOID AS
$$
BEGIN
EXECUTE format('TRUNCATE TABLE %I', table_name);
END;
$$ LANGUAGE plpgsql;
