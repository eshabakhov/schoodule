SELECT *
FROM public.school_class
WHERE {0}
ORDER BY school_class.grade ASC, school_class.litera ASC
LIMIT {1}
OFFSET {2}
