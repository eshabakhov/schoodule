UPDATE public.school
SET name = ?
WHERE school.id = ?
RETURNING id
