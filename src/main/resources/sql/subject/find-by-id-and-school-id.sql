SELECT *
FROM public.subject
WHERE ((subject.id = ?) AND (subject.school_id = ?)) AND (subject.is_deleted = ?)
