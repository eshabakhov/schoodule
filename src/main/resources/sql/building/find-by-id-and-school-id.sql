SELECT *
FROM public.building
WHERE ((building.id = ?) AND (building.school_id = ?)) AND (building.is_deleted = ?)
