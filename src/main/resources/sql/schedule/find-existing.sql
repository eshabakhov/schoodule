SELECT *
FROM public.schedule
WHERE ((schedule.school_id = ?) AND (schedule.name = ?)) AND (schedule.is_deleted = ?)
