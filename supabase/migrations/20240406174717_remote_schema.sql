create table "public"."play_evolutions" (
    "id" integer not null,
    "hash" character varying(255) not null,
    "applied_at" timestamp without time zone not null,
    "apply_script" text,
    "revert_script" text,
    "state" character varying(255),
    "last_problem" text
);


CREATE UNIQUE INDEX play_evolutions_pkey ON public.play_evolutions USING btree (id);

alter table "public"."play_evolutions" add constraint "play_evolutions_pkey" PRIMARY KEY using index "play_evolutions_pkey";

grant delete on table "public"."play_evolutions" to "anon";

grant insert on table "public"."play_evolutions" to "anon";

grant references on table "public"."play_evolutions" to "anon";

grant select on table "public"."play_evolutions" to "anon";

grant trigger on table "public"."play_evolutions" to "anon";

grant truncate on table "public"."play_evolutions" to "anon";

grant update on table "public"."play_evolutions" to "anon";

grant delete on table "public"."play_evolutions" to "authenticated";

grant insert on table "public"."play_evolutions" to "authenticated";

grant references on table "public"."play_evolutions" to "authenticated";

grant select on table "public"."play_evolutions" to "authenticated";

grant trigger on table "public"."play_evolutions" to "authenticated";

grant truncate on table "public"."play_evolutions" to "authenticated";

grant update on table "public"."play_evolutions" to "authenticated";

grant delete on table "public"."play_evolutions" to "service_role";

grant insert on table "public"."play_evolutions" to "service_role";

grant references on table "public"."play_evolutions" to "service_role";

grant select on table "public"."play_evolutions" to "service_role";

grant trigger on table "public"."play_evolutions" to "service_role";

grant truncate on table "public"."play_evolutions" to "service_role";

grant update on table "public"."play_evolutions" to "service_role";


