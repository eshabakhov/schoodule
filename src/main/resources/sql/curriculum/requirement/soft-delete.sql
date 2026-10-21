UPDATE public.federal_curriculum_requirement
SET is_deleted = ?, updated_at = CURRENT_TIMESTAMP
WHERE federal_curriculum_requirement.id = ?
