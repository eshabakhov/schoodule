UPDATE public.subscription
SET plan = ?, expires_at = NULL, updated_at = CAST(? AS timestamptz)
WHERE subscription.user_id = ?
