UPDATE public.school_class
SET grade = ?
WHERE school_class.id = ?
RETURNING id
