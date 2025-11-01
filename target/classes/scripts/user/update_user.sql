UPDATE users
SET
    name = COALESCE(?, name),
    password_hash = COALESCE(?, password_hash)
WHERE id = ?;