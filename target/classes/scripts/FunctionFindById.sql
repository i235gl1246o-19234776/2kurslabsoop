SELECT id, user_id, name, data_format, function_source, expression, parent_function_id, created_at
FROM functions
WHERE id = $1;