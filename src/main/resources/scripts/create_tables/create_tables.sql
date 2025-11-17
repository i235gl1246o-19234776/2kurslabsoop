CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS functions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type_function VARCHAR(20) CHECK (type_function IN ('tabular', 'analytic')),
    function_name VARCHAR(255) NOT NULL,
    function_expression TEXT
);

CREATE TABLE IF NOT EXISTS tabulated_functions (
    id SERIAL PRIMARY KEY,
    function_id INTEGER NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
    x_val DOUBLE PRECISION NOT NULL,
    y_val DOUBLE PRECISION NOT NULL
);

ALTER TABLE users ADD COLUMN role VARCHAR(20) CHECK (role IN ('ADMIN', 'USER')) NOT NULL DEFAULT 'USER';

CREATE TABLE IF NOT EXISTS operations (
    id SERIAL PRIMARY KEY,
    function_id INTEGER NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
    operations_type_id INTEGER NOT NULL
);