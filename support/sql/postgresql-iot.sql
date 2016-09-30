CREATE TABLE measure
(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    sensor_type VARCHAR(50) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    device_id VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    payload VARCHAR(255) NOT NULL,
    error_code INTEGER NOT NULL,
    error_message VARCHAR(255),
    time_stamp TIMESTAMP NOT NULL
);
ALTER TABLE measure OWNER TO :measureOwner