'use strict'

const Schema = use('Schema')

class VenuesTableSchema extends Schema {
  up () {
    this.create('venue_categories', (table) => {
      table.increments()
      table.string('name')
      table.string('plural_name')
      table.string('short_name')
      table.string('icon')
    })

    this.create('venues', (table) => {
      table.increments()
      table.string('name')
      table.text('description')
      table.float('lat', 10, 6)
      table.float('lng', 10, 6)
      table.text('address')
      table.text('url')
      table.specificType('price', 'smallint')
      table.integer('category').unsigned()
      table.foreign('category').references('venue_categories.id')
    })
  }

  down () {
    this.drop('venues')
    this.drop('venue_categories')
  }
}

module.exports = VenuesTableSchema
