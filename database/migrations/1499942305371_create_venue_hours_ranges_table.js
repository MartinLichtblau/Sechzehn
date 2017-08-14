'use strict'

const Schema = use('Schema')

class VenueHoursRangesTableSchema extends Schema {
  up () {
    this.create('venue_hours_ranges', (table) => {
      table.increments()
      table.string('venue_id', 60).notNullable()
      table.specificType('hours', 'tsrange')
      table.foreign('venue_id').references('venues.id')
    })

    this.raw('CREATE EXTENSION IF NOT EXISTS btree_gist')

    this.raw('ALTER TABLE venue_hours_ranges ADD CONSTRAINT hours_no_overlap EXCLUDE USING gist (venue_id with =, hours WITH &&)')
    this.raw('ALTER TABLE venue_hours_ranges ADD CONSTRAINT hours_bounds_inclusive CHECK (lower_inc(hours) AND upper_inc(hours))')
    this.raw('ALTER TABLE venue_hours_ranges ADD CONSTRAINT hours_standard_week CHECK (hours <@ tsrange \'[1996-01-01 0:0, 1996-01-08 0:0]\')')

    // Here comes the awesome stuff, see https://stackoverflow.com/a/22111524
    this.raw(
      `CREATE OR REPLACE FUNCTION f_normalize_time(timestamptz)
      RETURNS timestamp AS
      $func$
      SELECT date '1996-01-01' + ($1 AT TIME ZONE 'UTC' - date_trunc('week', $1 AT TIME ZONE 'UTC'))
      $func$  LANGUAGE sql IMMUTABLE;`
    )

    this.raw(
      `CREATE OR REPLACE FUNCTION f_venue_hours(_from timestamptz, _to timestamptz)
        RETURNS TABLE (venue_hours tsrange) AS
      $func$
      DECLARE
         ts_from timestamp := f_normalize_time(_from);
         ts_to   timestamp := f_normalize_time(_to);
      BEGIN
         -- test input for sanity (optional)
         IF _to <= _from THEN
            RAISE EXCEPTION '%', '_to must be later than _from!';
         ELSIF _to > _from + interval '1 week' THEN
            RAISE EXCEPTION '%', 'Interval cannot span more than a week!';
         END IF;
      
         IF ts_from > ts_to THEN  -- split range at Mon 00:00
            RETURN QUERY
            VALUES (tsrange('1996-01-01 0:0', ts_to  , '[]'))
                 , (tsrange(ts_from, '1996-01-08 0:0', '[]'));
         ELSE                     -- simple case: range in standard week
            venue_hours := tsrange(ts_from, ts_to, '[]');
            RETURN NEXT;
         END IF;
      
         RETURN;
      END
      $func$  LANGUAGE plpgsql IMMUTABLE COST 1000 ROWS 1;`
    )
  }

  down () {
    this.raw('DROP FUNCTION IF EXISTS f_normalize_time(timestamptz)')
    this.raw('DROP FUNCTION IF EXISTS f_venue_hours(_from timestamptz, _to timestamptz)')
    this.drop('venue_hours_ranges')
    this.raw('DROP EXTENSION IF EXISTS btree_gist')
  }
}

module.exports = VenueHoursRangesTableSchema
