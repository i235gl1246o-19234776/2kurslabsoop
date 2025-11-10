SELECT
    f.id AS function_id,
    f.user_id,
    u.name AS user_name,
    f.function_name,
    f.function_expression,
    f.type_function
FROM functions f
JOIN "USER" u ON f.user_id = u.id
WHERE 1=1
  AND (:user_id IS NULL OR f.user_id = :user_id)
  AND (:user_name IS NULL OR u.name ILIKE :user_name)
  AND (:function_name IS NULL OR f.function_name ILIKE :function_name)
  AND (:type_function IS NULL OR f.type_function = :type_function)