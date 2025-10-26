INSERT INTO functions (user_id, name, data_format, function_source, expression, parent_function_id)
VALUES ($1, $2, $3, $4, $5, $6)
RETURNING id, created_at;