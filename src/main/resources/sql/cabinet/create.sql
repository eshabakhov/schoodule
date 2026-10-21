INSERT INTO public.cabinet (building_id, name, is_deleted)
VALUES (?, ?, ?)
RETURNING *
