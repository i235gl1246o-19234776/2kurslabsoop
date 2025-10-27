UPDATE function_operations
SET result_value = $2,
    result_function_id = $3
WHERE id = $1;