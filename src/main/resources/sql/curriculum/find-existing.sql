SELECT *
FROM public.federal_curriculum
WHERE ((((federal_curriculum.education_level = CAST(? AS education_level_type)) AND
(federal_curriculum.study_week_type = CAST(? AS study_week_type))) AND
(federal_curriculum.version = ?)) AND (federal_curriculum.academic_year = ?)) AND
(federal_curriculum.is_deleted = ?)
