SELECT *
FROM public.cabinet
WHERE ((cabinet.building_id = ?) AND (cabinet.name = ?)) AND (cabinet.is_deleted = ?)
