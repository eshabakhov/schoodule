SELECT class_curriculum.id, school_class.id, subject.id, class_curriculum.hours_per_week
FROM public.class_curriculum
JOIN public.school_class ON class_curriculum.school_class_id = school_class.id
JOIN public.subject ON class_curriculum.subject_id = subject.id
WHERE class_curriculum.schedule_id = ?
