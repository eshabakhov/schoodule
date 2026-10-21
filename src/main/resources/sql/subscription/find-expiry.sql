SELECT subscription.expires_at
FROM public.subscription
WHERE subscription.user_id = ?
