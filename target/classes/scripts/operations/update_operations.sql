UPDATE operations
SET
    operations_type_id = COALESCE(?, operations_type_id)
WHERE id = ? AND function_id = ?;