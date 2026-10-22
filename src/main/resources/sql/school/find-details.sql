SELECT school.id, school.name
FROM public.school
WHERE school.id = ? AND school.is_deleted = ?
