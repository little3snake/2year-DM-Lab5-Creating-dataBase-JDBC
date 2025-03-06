CREATE OR REPLACE FUNCTION create_database_and_table(db_name VARCHAR)
RETURNS VOID AS
$$
BEGIN
    -- Создание базы данных
EXECUTE format('CREATE DATABASE IF NOT EXISTS %I', db_name);

-- Использование созданной базы данных не требуется в PostgreSQL, так как соединение всегда работает с одной базой данных.

-- Создание таблицы (если не существует)
EXECUTE format('
        CREATE TABLE IF NOT EXISTS cruises (
            id SERIAL PRIMARY KEY,
            LastName VARCHAR(255) NOT NULL,
            FirstName VARCHAR(255) NOT NULL,
            CruiseName VARCHAR(255) NOT NULL,
            DepartureDate DATE NOT NULL,
            DepartureCity VARCHAR(255) NOT NULL,
            ArrivalCity VARCHAR(255) NOT NULL
        )');
END;
$$ LANGUAGE plpgsql;
