SELECT *
FROM public.school
WHERE (school.name = ?) AND (school.is_deleted = ?)
