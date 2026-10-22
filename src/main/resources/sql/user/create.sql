INSERT INTO public."user" (username, password, email, corporate, school_id)
VALUES (?, ?, ?, ?, ?)
RETURNING *
