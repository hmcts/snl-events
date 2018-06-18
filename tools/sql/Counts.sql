select 'room' as entity, count(*) as rec_no  from room
union all
select 'person', count(*)  from person
union all
select 'availability', count(*)  from availability
union all
select 'session', count(*)  from session
union all
select 'hearing_part', count(*)  from hearing_part
union all
select 'problem', count(*)  from problem
union all
select 'problem_reference', count(*) from problem_reference
union all
select 'user_transaction', count(*)  from user_transaction
union all
select 'user_transaction_data', count(*)  from user_transaction_data;




