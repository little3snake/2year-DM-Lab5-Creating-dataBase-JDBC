CREATE OR REPLACE FUNCTION update_cruise(
    cruise_id INT,
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
UPDATE cruises
SET LastName = last_name,
    FirstName = first_name,
    CruiseName = cruise_name,
    DepartureDate = departure_date,
    DepartureCity = departure_city,
    ArrivalCity = arrival_city
WHERE id = cruise_id;
END;
$$ LANGUAGE plpgsql;
