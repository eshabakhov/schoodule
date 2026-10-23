UPDATE public.subject
SET name = ?
WHERE subject.id = ? AND subject.is_deleted = ?
RETURNING id
