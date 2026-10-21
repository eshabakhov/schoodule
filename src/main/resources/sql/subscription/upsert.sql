INSERT INTO public.subscription (user_id, plan, expires_at, updated_at)
VALUES (?, ?, CAST(? AS timestamptz), CAST(? AS timestamptz))
ON CONFLICT (user_id)
DO UPDATE SET plan = ?, expires_at = CAST(? AS timestamptz), updated_at = CAST(? AS timestamptz)
