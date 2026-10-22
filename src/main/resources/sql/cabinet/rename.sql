UPDATE public.cabinet
SET name = ?
WHERE cabinet.id = ?
RETURNING id
