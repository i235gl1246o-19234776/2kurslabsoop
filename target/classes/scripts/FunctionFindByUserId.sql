SELECT id, name, data_format, function_source, expression, parent_function_id, created_at
FROM functions
WHERE user_id = $1
ORDER BY created_at DESC;