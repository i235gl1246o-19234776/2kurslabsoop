UPDATE functions
SET
    type_function = COALESCE(?, type_function),
    function_name = COALESCE(?, function_name),
    function_expression = COALESCE(?, function_expression)
WHERE id = ? AND user_id = ?;