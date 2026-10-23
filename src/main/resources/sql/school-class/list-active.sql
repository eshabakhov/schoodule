SELECT *
FROM public.school_class
WHERE school_class.school_id = ? AND school_class.is_deleted = ?
ORDER BY school_class.grade ASC, school_class.litera ASC, school_class.id ASC
