SELECT id, function_id, operation_type_id, parameters, result_value, executed_at
FROM function_operations
WHERE result_function_id = $1;