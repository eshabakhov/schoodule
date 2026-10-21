UPDATE public.federal_curriculum
SET title = ?, education_level = CAST(? AS education_level_type), study_week_type =
CAST(? AS study_week_type), version = ?, academic_year = ?, description = ?
WHERE federal_curriculum.id = ?
RETURNING id
