CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS operation_types (
    id SMALLINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    result_type VARCHAR(10) NOT NULL CHECK (result_type IN ('num', 'function'))
);

CREATE TABLE IF NOT EXISTS functions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100),
    data_format VARCHAR(20) NOT NULL CHECK (data_format IN ('analytical', 'tabulated')),
    function_source VARCHAR(20) NOT NULL CHECK (function_source IN ('base', 'derived', 'composite')),
    expression TEXT,
    parent_function_id BIGINT REFERENCES functions(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS function_points (
    id BIGSERIAL PRIMARY KEY,
    function_id BIGINT NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
    x_val DECIMAL(20, 12) NOT NULL,
    y_val DOUBLE PRECISION NOT NULL,
    computed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(function_id, x_val)
);

CREATE TABLE IF NOT EXISTS function_operations (
    id BIGSERIAL PRIMARY KEY,
    function_id BIGINT NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
    operation_type_id SMALLINT NOT NULL REFERENCES operation_types(id),
    parameters JSONB,
    result_value DOUBLE PRECISION,
    result_function_id BIGINT REFERENCES functions(id) ON DELETE SET NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);