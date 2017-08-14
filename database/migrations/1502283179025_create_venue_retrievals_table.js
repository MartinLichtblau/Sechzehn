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
    })
  }

  down () {
    this.drop('venue_retrievals')
  }
}

module.exports = VenueRetrievalsTableSchema
