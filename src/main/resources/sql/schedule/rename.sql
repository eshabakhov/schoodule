UPDATE public.schedule
SET name = ?
WHERE schedule.id = ? AND schedule.is_deleted = ?
RETURNING id
