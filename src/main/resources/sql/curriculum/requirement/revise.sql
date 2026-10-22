UPDATE public.federal_curriculum_requirement
SET grade = ?, subject_name = ?, weekly_hours = ?, part_type = CAST(? AS
curriculum_part_type)
WHERE federal_curriculum_requirement.id = ?
RETURNING id
