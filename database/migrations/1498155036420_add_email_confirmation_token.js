'use strict'

const Schema = use('Schema')

class UsersTableSchema extends Schema {
  up () {
    this.table('users', (table) => {
      table.boolean('confirmed').defaultTo(false)
      table.string('confirmation_token', 16).nullable()
    })
  }

  down () {
    this.table('users', (table) => {
      table.dropColumn('confirmed')
      table.dropColumn('confirmation_token')
    })
  }
}

module.exports = UsersTableSchema
