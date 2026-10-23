SELECT subject.id
FROM public.subject
WHERE (subject.school_id = ?) AND (subject.is_deleted = ?)
ORDER BY subject.name ASC, subject.id ASC
