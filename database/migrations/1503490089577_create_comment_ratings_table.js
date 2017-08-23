'use strict'

const Schema = use('Schema')

class CommentRatingsTableSchema extends Schema {
  up () {
    this.create('comment_ratings', (table) => {
      table.increments()
      table.timestamps()
      table.boolean('thumbs_up')
      table.string('username', 80).notNullable()
      table.integer('comment_id').unsigned().notNullable()
      table.foreign('username').references('users.username')
      table.foreign('comment_id').references('comments.id')
    })
  }

  down () {
    this.drop('comment_ratings')
  }
}

module.exports = CommentRatingsTableSchema
