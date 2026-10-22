SELECT class_curriculum.id, class_curriculum.school_class_id,
class_curriculum.subject_id, class_curriculum.hours_per_week
FROM public.class_curriculum
WHERE class_curriculum.id = ?
