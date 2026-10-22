SELECT *
FROM public.schedule
WHERE {0}
ORDER BY schedule.name ASC
LIMIT {1}
OFFSET {2}
