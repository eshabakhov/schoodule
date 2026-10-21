UPDATE public.federal_curriculum
SET is_deleted = ?, updated_at = CURRENT_TIMESTAMP
WHERE federal_curriculum.id = ?
