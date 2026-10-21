SELECT *
FROM public.federal_curriculum_requirement
WHERE ((((federal_curriculum_requirement.federal_curriculum_id = ?) AND
(federal_curriculum_requirement.grade = ?)) AND
(federal_curriculum_requirement.subject_name = ?)) AND
(federal_curriculum_requirement.part_type = CAST(? AS curriculum_part_type))) AND
(federal_curriculum_requirement.is_deleted = ?)
