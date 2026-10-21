UPDATE public.school_class
SET litera = ?
WHERE school_class.id = ?
RETURNING id
