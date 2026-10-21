INSERT INTO public.teacher (school_id, name, is_deleted)
VALUES (?, ?, ?)
RETURNING *
