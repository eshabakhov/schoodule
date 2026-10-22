UPDATE public.schedule
SET name = ?
WHERE schedule.id = ?
RETURNING id
