'use strict'

const Schema = use('Schema')

class UsersTableSchema extends Schema {
  up () {
    this.table('users', (table) => {
      table.boolean('incognito').defaultTo(false)
    })
  }

  down () {
    this.table('users', (table) => {
      table.dropColumn('incognito')
    })
  }
}

module.exports = UsersTableSchema
