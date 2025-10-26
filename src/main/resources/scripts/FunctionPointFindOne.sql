SELECT x_val, y_val, computed_at
FROM function_points
WHERE function_id = $1 AND x_val = $2;