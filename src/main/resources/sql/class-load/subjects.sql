SELECT id, name FROM public.subject
WHERE subject.school_id = ? AND subject.is_deleted = false
ORDER BY subject.name, subject.id
