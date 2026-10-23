UPDATE public.teacher
SET name = ?
WHERE teacher.id = ? AND teacher.is_deleted = ?
RETURNING id
