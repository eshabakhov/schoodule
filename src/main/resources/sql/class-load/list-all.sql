SELECT class_curriculum.*
FROM public.class_curriculum
JOIN public.school_class ON school_class.id = class_curriculum.school_class_id
JOIN public.subject ON subject.id = class_curriculum.subject_id
WHERE class_curriculum.schedule_id = ?
ORDER BY school_class.grade, school_class.litera, subject.name, class_curriculum.id
