INSERT INTO public.federal_curriculum_requirement (federal_curriculum_id, grade,
subject_name, weekly_hours, part_type, is_deleted)
VALUES (?, ?, ?, ?, CAST(? AS curriculum_part_type), ?)
RETURNING *
