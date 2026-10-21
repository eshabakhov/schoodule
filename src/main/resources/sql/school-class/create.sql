INSERT INTO public.school_class (school_id, grade, litera, is_deleted)
VALUES (?, ?, ?, ?)
RETURNING *
