INSERT INTO function_operations (
    function_id,
    operation_type_id,
    parameters,
    result_value,
    result_function_id
)
VALUES ($1, $2, $3, $4, $5)
RETURNING id, executed_at;