INSERT INTO public.schedule (school_id, name, is_deleted)
VALUES (?, ?, ?)
RETURNING *
