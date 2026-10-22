SELECT *
FROM public.school
WHERE {0}
ORDER BY school.name ASC
LIMIT {1}
OFFSET {2}
