update hearing set number_of_sessions = (SELECT COUNT(*) from hearing_part where hearing_part.hearing_id = id);
update hearing set is_multisession = CASE WHEN duration >= 86400000 THEN true ELSE false END;
