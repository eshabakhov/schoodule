INSERT INTO public.subscription (user_id, plan, expires_at)
VALUES (?, ?, NULL)
ON CONFLICT (user_id) DO NOTHING
