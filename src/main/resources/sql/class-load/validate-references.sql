SELECT school_class.id, subject.id
FROM public.schedule
JOIN public.school_class ON school_class.school_id = schedule.school_id
JOIN public.subject ON subject.school_id = schedule.school_id
WHERE schedule.id = ? AND schedule.is_deleted = false
AND school_class.id = ? AND school_class.is_deleted = false
AND subject.id = ? AND subject.is_deleted = false
