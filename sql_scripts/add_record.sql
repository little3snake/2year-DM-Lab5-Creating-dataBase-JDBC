CREATE OR REPLACE FUNCTION add_record(
    last_name VARCHAR,
    first_name VARCHAR,
    cruise_name VARCHAR,
    departure_date DATE,
    departure_city VARCHAR,
    arrival_city VARCHAR
)
RETURNS VOID AS
$$
BEGIN
INSERT INTO cruises (LastName, FirstName, CruiseName, DepartureDate, DepartureCity, ArrivalCity)
VALUES (last_name, first_name, cruise_name, departure_date, departure_city, arrival_city);
END;
$$ LANGUAGE plpgsql;
