DELETE FROM public.class_curriculum
WHERE class_curriculum.id = ? AND class_curriculum.schedule_id = ?
RETURNING id
