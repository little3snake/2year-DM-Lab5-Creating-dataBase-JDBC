CREATE OR REPLACE FUNCTION search_cruise(search_term VARCHAR)
RETURNS TABLE (
    id INT,
    LastName VARCHAR,
    FirstName VARCHAR,
    CruiseName VARCHAR,
    DepartureDate DATE,
    DepartureCity VARCHAR,
    ArrivalCity VARCHAR
) AS
$$
BEGIN
RETURN QUERY
SELECT
    cruises.id,          -- Явно указываем таблицу для столбца id
    cruises.LastName,
    cruises.FirstName,
    cruises.CruiseName,
    cruises.DepartureDate,
    cruises.DepartureCity,
    cruises.ArrivalCity
FROM cruises
WHERE
    --cruises.LastName ILIKE '%' || search_term || '%'
    cruises.LastName ~* ('(^|\\s)' || search_term || '(\\s|$)')
        --OR cruises.FirstName ILIKE '%' || search_term || '%'
    OR cruises.FirstName ~* ('(^|\\s)' || search_term || '(\\s|$)')
        --OR cruises.CruiseName ILIKE '%' || search_term || '%'
    OR cruises.CruiseName ~* ('(^|\\s)' || search_term || '(\\s|$)')
        --OR cruises.DepartureCity ILIKE '%' || search_term || '%'
    OR cruises.DepartureCity ~* ('(^|\\s)' || search_term || '(\\s|$)')
        --OR cruises.ArrivalCity ILIKE '%' || search_term || '%';
    OR cruises.ArrivalCity ~* ('(^|\\s)' || search_term || '(\\s|$)');
END;
$$ LANGUAGE plpgsql;