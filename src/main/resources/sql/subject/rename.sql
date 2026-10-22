UPDATE public.subject
SET name = ?
WHERE subject.id = ?
RETURNING id
