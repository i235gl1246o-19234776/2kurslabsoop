SELECT id, operation_type_id, parameters, result_value, result_function_id, executed_at
FROM function_operations
WHERE function_id = $1
ORDER BY executed_at DESC;