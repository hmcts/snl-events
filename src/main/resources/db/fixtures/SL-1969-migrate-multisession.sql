update hearing set is_multisession = CASE WHEN duration >= 86400000 THEN true ELSE false END;
update hearing_aud set is_multisession = CASE WHEN duration >= 86400000 THEN true ELSE false END;
