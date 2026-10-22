INSERT INTO public.class_curriculum (schedule_id, school_class_id, subject_id,
hours_per_week)
VALUES (?, ?, ?, ?)
RETURNING *
