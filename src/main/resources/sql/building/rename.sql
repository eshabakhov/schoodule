UPDATE public.building
SET name = ?
WHERE (building.id = ?) AND (building.is_deleted = ?)
RETURNING id
