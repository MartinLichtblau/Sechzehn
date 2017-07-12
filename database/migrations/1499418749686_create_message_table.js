'use strict'

const Schema = use('Schema')

class MessagesTableSchema extends Schema {
  up () {
    this.create('messages', (table) => {
      table.increments()
      table.timestamps()
      table.string('sender', 80)
      table.string('receiver', 80)
      table.text('body')
      table.boolean('is_read').defaultTo(false)
      table.foreign('sender').references('users.username')
      table.foreign('receiver').references('users.username')
    })
  }

  down () {
    this.drop('messages')
  }
}

module.exports = MessagesTableSchema
