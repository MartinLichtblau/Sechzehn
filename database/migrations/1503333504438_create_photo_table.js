'use strict'

const Schema = use('Schema')

class PhotosTableSchema extends Schema {
  up () {
    this.create('photos', (table) => {
      table.increments()
      table.timestamps()
      table.string('venue_id', 60).notNullable()
      table.string('username', 80).notNullable()
      table.foreign('venue_id').references('venues.id')
      table.foreign('username').references('users.username')
    })
  }

  down () {
    this.drop('photos')
  }
}

module.exports = PhotosTableSchema
