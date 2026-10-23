SELECT teacher.id
FROM public.teacher
WHERE (teacher.school_id = ?) AND (teacher.is_deleted = ?)
ORDER BY teacher.name ASC, teacher.id ASC
