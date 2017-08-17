'use strict'

const Schema = use('Schema')

class VenueRetrievalsTableSchema extends Schema {
  up () {
    this.create('venue_retrievals', (table) => {
      table.increments()
      table.timestamps()
      table.float('lat', 10, 6)
      table.float('lng', 10, 6)
      table.integer('radius').unsigned()
      table.string('section', 15)
    })
    this.create('venue_section', table => {
      table.string('venue_id', 60).notNullable()
      table.string('section', 15).notNullable()
      table.foreign('venue_id').references('venues.id')
      table.primary(['venue_id', 'section'])
    })
  }

  down () {
    this.drop('venue_retrievals')
    this.drop('venue_section')
  }
}

module.exports = VenueRetrievalsTableSchema
