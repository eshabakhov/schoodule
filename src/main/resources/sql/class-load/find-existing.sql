SELECT *
FROM public.class_curriculum
WHERE ((class_curriculum.schedule_id = ?) AND (class_curriculum.school_class_id = ?))
AND (class_curriculum.subject_id = ?)
