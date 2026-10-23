UPDATE public.class_curriculum
SET school_class_id = ?, subject_id = ?, hours_per_week = ?
WHERE class_curriculum.id = ? AND class_curriculum.schedule_id = ?
RETURNING id
