UPDATE public.school
SET name = ?
WHERE school.id = ? AND school.is_deleted = ?
RETURNING id
