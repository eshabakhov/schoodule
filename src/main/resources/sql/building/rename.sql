UPDATE public.building
SET name = ?
WHERE building.id = ?
RETURNING id
