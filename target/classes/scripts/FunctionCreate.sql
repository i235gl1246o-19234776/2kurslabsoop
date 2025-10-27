INSERT INTO functions (user_id, name, data_format, function_source, expression, parent_function_id)
VALUES (?, ?, ?, ?, ?, ?)
RETURNING id, created_at;