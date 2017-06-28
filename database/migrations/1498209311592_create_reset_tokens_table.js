'use strict'

const Schema = use('Schema')

class ResetTokensTableSchema extends Schema {

  up () {
    this.create('reset_tokens', (table) => {
      table.string('user', 80)
      table.foreign('user').references('users.username')
      table.string('token').unique().primary()
      table.timestamp('created_at')
    })
  }

  down () {
    this.drop('reset_tokens')
  }

}

module.exports = ResetTokensTableSchema
