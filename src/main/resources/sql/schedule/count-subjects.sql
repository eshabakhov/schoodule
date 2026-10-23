SELECT count(DISTINCT class_curriculum.subject_id)
FROM public.class_curriculum
WHERE class_curriculum.schedule_id = ?
