CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
/*
	Generates data into the database for testing performance of the SnL Poc.
	Settings to change the volume of data are below;
 */

-- settings
DECLARE 
	numberOfJudges int := 50;
	numberOfRooms int := numberOfJudges; -- at present the number of rooms and judges needs to be the same

	startDateTime timestamp with time zone := '2018-06-01 07:00:00+00';
	numberOfWorkingDaysToGenerate int := 92;
	availaiblitySecondsPerDay int := 8 * 60 * 60; --8h

	numberOfSessionsPerDay int := 2;
	durationOfSessionInSeconds int := 3 * 60 * 60; --3h

	numberOfHearingsPartsPerSession int := 3;
	durationOfHearingsPartInSeconds int := 1 * 60 * 60; --1h
-- so we have 8h availability, 2-3h sessions in, and then in session 6-0.5h hearings === means "perfectly 100% booked"

-- temp variables below
DECLARE 
	temp_id uuid;
	temp_dt timestamp with time zone;
	temp_dt2 timestamp with time zone;
	temp_dt3 timestamp with time zone;
	i_days int;
    cur_judges cursor for
		select id from person where person_type = 'judge';
	rec_judge record;
	cur_rooms cursor for
		select id from room;
	rec_room record;
	cur_sessions cursor for
		select id, start from session;
	rec_session record;
BEGIN
	RAISE NOTICE 'add persons-judges with availability';
	FOR counter IN 1..numberOfJudges LOOP
		temp_id := uuid_generate_v4();
		insert into person(id, person_type, name, username)
		values (temp_id, 'judge', 'judge_' || counter, 'judge_' || counter);
		
		-- add availability for it
		temp_dt := startDateTime;
		FOR counter IN 1..numberOfWorkingDaysToGenerate LOOP
			IF (extract(dow from temp_dt) NOT IN (0,5,6)) THEN
				INSERT INTO availability (id, start, duration, person_id, room_id ) 
				values (uuid_generate_v4(), temp_dt, availaiblitySecondsPerDay, temp_id, null);
			END IF;
			temp_dt = temp_dt + interval '1' day;
		END LOOP;
	END LOOP;
	
	RAISE NOTICE 'add rooms with availability';
	FOR counter IN 1..numberOfRooms LOOP
		temp_id := uuid_generate_v4();
		insert into room(id, name)
		values (temp_id, 'room_' || counter);
		
		-- add availability for it
		temp_dt := startDateTime;
		FOR counter IN 1..numberOfWorkingDaysToGenerate LOOP
			IF (extract(dow from temp_dt) NOT IN (0,5,6)) THEN
				INSERT INTO availability (id, start, duration, person_id, room_id ) 
				values (uuid_generate_v4(), temp_dt, availaiblitySecondsPerDay, null, temp_id);
			END IF;
			temp_dt = temp_dt + interval '1' day;
		END LOOP;
	END LOOP;

	RAISE NOTICE 'add sessions';
	-- add sessions, all session will have judge and room provided, so it is not double booked.
	-- for each judge create session for each day
	temp_dt := startDateTime;
	i_days := 1;
	while i_days <= numberOfWorkingDaysToGenerate LOOP
		open cur_judges;
		open cur_rooms;
		loop
			fetch cur_judges into rec_judge;
			exit when not found;
			fetch cur_rooms into rec_room;
			exit when not found;
		
			FOR counter IN 1..numberOfSessionsPerDay LOOP
				temp_dt2 := temp_dt + interval '1' second * durationOfSessionInSeconds * counter;
				IF (extract(dow from temp_dt2) NOT IN (0,5,6)) THEN
					insert into session (id, person_id, room_id, start, duration, case_type)
					values (uuid_generate_v4(), rec_judge.id, rec_room.id,  temp_dt2, durationOfSessionInSeconds, 'FTRACK');
				END IF;
			END LOOP;
		end loop;
		close cur_judges;
		close cur_rooms;
		temp_dt = temp_dt + interval '1' day;
		i_days := i_days + 1;
	end loop;
	
	RAISE NOTICE 'add hearing parts';
	-- add hearing parts
	temp_dt := startDateTime;
	open cur_sessions;
	loop
		fetch cur_sessions into rec_session;
		exit when not found;
		
		FOR counter IN 1..numberOfHearingsPartsPerSession LOOP
			temp_dt2 := rec_session.start + interval '1' second * durationOfHearingsPartInSeconds * counter;
			temp_dt3 := rec_session.start + interval '1' second * (durationOfHearingsPartInSeconds * counter + durationOfHearingsPartInSeconds);
			insert into hearing_part (id, session_id, case_number, case_title, case_type, hearing_type, duration, schedule_start, schedule_end, start, created_at)
			values (uuid_generate_v4(), rec_session.id, 'cn' || counter,  'ct' || counter, 'FTRACK', 'Preliminary Hearing', durationOfHearingsPartInSeconds, temp_dt2, temp_dt3, temp_dt2, startDateTime);
		END LOOP;
		
		temp_dt = temp_dt + interval '1' day;
	end loop;
	close cur_sessions;
END; $$
