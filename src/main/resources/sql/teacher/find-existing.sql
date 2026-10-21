SELECT *
FROM public.teacher
WHERE ((teacher.school_id = ?) AND (teacher.name = ?)) AND (teacher.is_deleted = ?)
