SELECT *
FROM public.teacher
WHERE {0}
ORDER BY teacher.name ASC
LIMIT {1}
OFFSET {2}
