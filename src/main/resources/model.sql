-- -*- sql-product: sqlite; -*-

pragma foreign_keys = false;

drop table if exists building;
drop table if exists floor;
drop table if exists elevator;
drop table if exists route;
drop table if exists elevator_route;
drop table if exists clock;

pragma foreign_keys = true;

create table if not exists building (
       id integer primary key,
       name text
);

create table if not exists floor (
       id integer primary key,
       building_id references building,
       floor_number integer not null default 0,
       unique (building_id, floor_number)
);
       
create table if not exists elevator (
       id integer primary key,
       floor_id references floor,
       direction integer not null default 0 check (direction in (-1, 0, 1))
);

create table if not exists route (
       id integer primary key,
       start_floor_id references floor,
       end_floor_id references floor,
       unique (start_floor_id, end_floor_id)
);

create table if not exists elevator_route (
       id integer primary key,
       elevator_id references elevator,
       route_id references route,
       unique (route_id)
);

-- --------------------------------------------------------------------------------

drop view if exists v_route_ext;
create view if not exists v_route_ext as
select r.*,
        fs.floor_number as start_floor,
        fe.floor_number as end_floor,
        abs(fe.floor_number - fs.floor_number) as distance,
        (fe.floor_number - fs.floor_number)/abs(fe.floor_number - fs.floor_number) as direction,
        er.elevator_id as elevator_id,
        ef.floor_number as elevator_floor
from route r
inner join floor fs on fs.id = r.start_floor_id
inner join floor fe on fe.id = r.end_floor_id
left outer join elevator_route er on er.route_id = r.id
left outer join elevator e on e.id = er.elevator_id
left outer join floor ef on ef.id = e.floor_id;



drop view if exists v_elevator_stop;
create view if not exists v_elevator_stop as
select
	e.id,
	f.id as floor_id,
	f.floor_number
from elevator e
left outer join elevator_route er on er.elevator_id = e.id
left outer join route r on r.id = er.route_id
left outer join floor f on f.id = r.start_floor_id
union
select
	e.id,
	f.id as floor_id,
	f.floor_number
from elevator e
left outer join elevator_route er on er.elevator_id = e.id
left outer join route r on r.id = er.route_id
left outer join floor f on f.id = r.end_floor_id;

drop view if exists v_elevator_ext;
create view if not exists v_elevator_ext as
select
	e.*,
	f.floor_number,
	(select count(*) from v_elevator_stop es where es.id = e.id and floor_number >= f.floor_number) as ustops,
	(select count(*) from v_elevator_stop es where es.id = e.id and floor_number <  f.floor_number) as dstops
from elevator e
inner join floor f on f.id = e.floor_id;

-- --------------------------------------------------------------------------------

drop view if exists v_route_unassigned;
create view if not exists v_route_unassigned as
select * from v_route_ext r where not exists (select * from elevator_route where route_id = r.id);

drop view if exists v_leaderboard;
create view if not exists v_leaderboard as
select
	ru.id as route_id,
	e.id as elevator_id,
	case ru.direction*e.direction
	     when 1 then 1*abs(ef.floor_number - rsf.floor_number)
	     when 0 then 10*abs(ef.floor_number - rsf.floor_number)
	     else 100*abs(ef.floor_number - rsf.floor_number) end + (select h.id + e.id from v_clocktick h) % (select count(*) from elevator) as score
from elevator e, v_route_unassigned ru
inner join floor ef on ef.id = e.floor_id
inner join floor rsf on rsf.id = ru.start_floor_id
inner join floor ref on ref.id = ru.end_floor_id
left outer join elevator_route er on er.route_id = ru.id;

drop view if exists v_best_score;
create view if not exists v_best_score as
select
	l.route_id,
	min(score) as score
from v_leaderboard l
group by l.route_id;

drop view if exists v_best_elevator;
create view if not exists v_best_elevator as
select
	l.route_id,
	l.elevator_id
from v_leaderboard l
inner join v_best_score bs on bs.score = l.score;

-- --------------------------------------------------------------------------------

create table if not exists clock (
       id integer primary key
);

drop view if exists v_clocktick;
create view if not exists v_clocktick as
select id from clock order by id desc limit 1;

drop trigger if exists t_ticktock;
create trigger if not exists t_ticktock insert on clock
begin
insert into elevator_route (elevator_id, route_id) select elevator_id, route_id from v_best_elevator;
end;

drop view if exists v_display;
create view if not exists v_display as
select
	id as '',
	replace(substr(quote(zeroblob((floor_number + 1) / 2)), 3, floor_number), '0', ' ') || char(0x2588) as ' '
from v_elevator_ext ee;
