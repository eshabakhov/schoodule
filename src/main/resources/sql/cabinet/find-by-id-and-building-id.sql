SELECT *
FROM public.cabinet
WHERE ((cabinet.id = ?) AND (cabinet.building_id = ?)) AND (cabinet.is_deleted = ?)
