SELECT id, function_id, operation_type_id, parameters, result_value, result_function_id, executed_at
FROM function_operations
WHERE id = $1;