UPDATE public.cabinet
SET name = ?
WHERE (cabinet.id = ?) AND (cabinet.is_deleted = ?)
RETURNING id
