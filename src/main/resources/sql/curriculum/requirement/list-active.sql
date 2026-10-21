SELECT *
FROM public.federal_curriculum_requirement
WHERE (federal_curriculum_requirement.federal_curriculum_id = ?) AND
(federal_curriculum_requirement.is_deleted = ?)
