SELECT schedule.id
FROM public.schedule
WHERE (schedule.school_id = ?) AND (schedule.is_deleted = ?)
ORDER BY schedule.name ASC, schedule.id ASC
