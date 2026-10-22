SELECT *
FROM public."user"
WHERE ("user".id = ?) AND ("user".deleted IS NULL)
