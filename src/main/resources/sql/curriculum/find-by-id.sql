SELECT *
FROM public.federal_curriculum
WHERE (federal_curriculum.id = ?) AND (federal_curriculum.is_deleted = ?)
