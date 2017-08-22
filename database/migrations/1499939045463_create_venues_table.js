'use strict'

const Schema = use('Schema')

class VenuesTableSchema extends Schema {
  up () {
    this.create('venue_categories', (table) => {
      table.string('id', 60).primary()
      table.string('name')
      table.string('plural_name')
      table.string('short_name')
      table.string('icon')
    })

    this.create('venues', (table) => {
      table.string('id', 60).primary()
      table.string('name')
      table.text('description')
      table.float('lat', 10, 6)
      table.float('lng', 10, 6)
      table.text('address')
      table.string('url')
      table.string('phone')
      table.specificType('price', 'smallint')
      table.float('foursquare_rating', 4, 2)
      table.integer('foursquare_rating_count')
      table.boolean('details_fetched').defaultTo(false)
      table.string('category_id', 60)
      table.foreign('category_id').references('venue_categories.id')
    })
  }

  down () {
    this.drop('venues')
    this.drop('venue_categories')
  }
}

module.exports = VenuesTableSchema
