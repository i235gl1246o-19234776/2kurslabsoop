UPDATE functions
SET name = $2,
    data_format = $3,
    function_source = $4,
    expression = $5,
    parent_function_id = $6
WHERE id = $1;