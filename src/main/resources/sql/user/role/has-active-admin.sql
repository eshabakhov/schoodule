SELECT EXISTS (SELECT 1
FROM public.user_role
JOIN public.role ON user_role.role_id = role.id
JOIN public."user" ON user_role.user_id = "user".id
WHERE (role.name = CAST(? AS role_type)) AND ("user".deleted IS NULL))
