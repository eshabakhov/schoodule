INSERT INTO public.building (school_id, name, is_deleted)
VALUES (?, ?, ?)
RETURNING *
