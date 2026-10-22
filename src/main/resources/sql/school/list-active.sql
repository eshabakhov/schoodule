SELECT school.id
FROM public.school
WHERE school.is_deleted = ?
ORDER BY school.name ASC, school.id ASC
