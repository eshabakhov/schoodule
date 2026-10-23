SELECT count(*)
FROM public.school_class
WHERE school_class.school_id = ? AND school_class.is_deleted = ?
