SELECT role.name
FROM public.role
JOIN public.user_role ON role.id = user_role.role_id
WHERE user_role.id = ?
