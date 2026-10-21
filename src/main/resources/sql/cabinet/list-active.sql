SELECT cabinet.id
FROM public.cabinet
WHERE (cabinet.building_id = ?) AND (cabinet.is_deleted = ?)
ORDER BY cabinet.name ASC, cabinet.id ASC
