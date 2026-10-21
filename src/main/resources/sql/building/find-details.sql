SELECT building.id, building.name
FROM public.building
WHERE (building.id = ?) AND (building.is_deleted = ?)
