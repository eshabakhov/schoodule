UPDATE public.school_class
SET grade = ?, litera = ?
WHERE school_class.id = ? AND school_class.is_deleted = ?
RETURNING id
