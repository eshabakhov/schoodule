SELECT *
FROM public.building
WHERE ((building.school_id = ?) AND (building.name = ?)) AND (building.is_deleted = ?)
