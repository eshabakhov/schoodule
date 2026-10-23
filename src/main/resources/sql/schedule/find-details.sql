SELECT *
FROM public.schedule
WHERE schedule.id = ? AND schedule.is_deleted = ?
