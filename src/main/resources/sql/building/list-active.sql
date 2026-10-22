SELECT building.id
FROM public.building
WHERE (building.school_id = ?) AND (building.is_deleted = ?)
ORDER BY building.name ASC, building.id ASC
