UPDATE public.teacher
SET name = ?
WHERE teacher.id = ?
RETURNING id
