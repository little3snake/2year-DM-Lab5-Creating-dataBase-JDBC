CREATE OR REPLACE FUNCTION delete_record(search_term VARCHAR)
RETURNS VOID AS
$$
BEGIN
EXECUTE format('
        DELETE FROM cruises
        WHERE LastName LIKE %L
           OR FirstName LIKE %L
           OR CruiseName LIKE %L
           OR DepartureCity LIKE %L
           OR ArrivalCity LIKE %L',
               '%' || search_term || '%', -- Добавление символов '%' для поиска
               '%' || search_term || '%',
               '%' || search_term || '%',
               '%' || search_term || '%',
               '%' || search_term || '%');
END;
$$ LANGUAGE plpgsql;
