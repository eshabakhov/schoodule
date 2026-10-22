SELECT *
FROM public.school_class
WHERE (((school_class.school_id = ?) AND (school_class.grade = ?)) AND
(school_class.litera = ?)) AND (school_class.is_deleted = ?)
