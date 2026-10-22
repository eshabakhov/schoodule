INSERT INTO public.federal_curriculum (title, education_level, study_week_type, version,
academic_year, description, is_deleted)
VALUES (?, CAST(? AS education_level_type), CAST(? AS study_week_type), ?, ?, ?, ?)
RETURNING *
