'use strict'

const Schema = use('Schema')

class CommentsTableSchema extends Schema {
  up () {
    this.create('comments', (table) => {
      table.increments()
      table.timestamps()
      table.text('body').notNullable()
      table.string('venue_id', 60).notNullable()
      table.string('username', 80).notNullable()
      table.integer('photo_id').unsigned()
      table.foreign('venue_id').references('venues.id')
      table.foreign('username').references('users.username')
      table.foreign('photo_id').references('photos.id')
    })
  }

  down () {
    this.drop('comments')
  }
}

module.exports = CommentsTableSchema
