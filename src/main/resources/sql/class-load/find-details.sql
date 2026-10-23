SELECT class_curriculum.id, class_curriculum.schedule_id, class_curriculum.school_class_id,
class_curriculum.subject_id, class_curriculum.hours_per_week,
school_class.grade AS class_grade, school_class.litera AS class_litera,
subject.name AS subject_name
FROM public.class_curriculum
JOIN public.school_class ON school_class.id = class_curriculum.school_class_id
JOIN public.subject ON subject.id = class_curriculum.subject_id
WHERE class_curriculum.id = ?
