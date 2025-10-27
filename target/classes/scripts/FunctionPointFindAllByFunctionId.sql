SELECT x_val, y_val, computed_at
FROM function_points
WHERE function_id = $1
ORDER BY x_val;