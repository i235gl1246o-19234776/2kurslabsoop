UPDATE tabulated_functions
SET
    x_val = COALESCE(?, x_val),
    y_val = COALESCE(?, y_val)
WHERE id = ? AND function_id = ?;