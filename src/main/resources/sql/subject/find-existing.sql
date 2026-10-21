SELECT *
FROM public.subject
WHERE ((subject.school_id = ?) AND (subject.name = ?)) AND (subject.is_deleted = ?)
