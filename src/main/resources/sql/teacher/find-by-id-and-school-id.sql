SELECT *
FROM public.teacher
WHERE ((teacher.id = ?) AND (teacher.school_id = ?)) AND (teacher.is_deleted = ?)
