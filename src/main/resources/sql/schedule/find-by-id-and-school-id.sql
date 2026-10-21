SELECT *
FROM public.schedule
WHERE ((schedule.id = ?) AND (schedule.school_id = ?)) AND (schedule.is_deleted = ?)
