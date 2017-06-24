'use strict'

const Schema = use('Schema')

class ResetTokensTableSchema extends Schema {

  up () {
    this.create('reset_tokens', (table) => {
      table.integer('user_id').unsigned()
      table.foreign('user_id').references('users.id')
      table.string('token').unique().primary()
      table.timestamp('created_at')
    })
  }

  down () {
    this.drop('reset_tokens')
  }

}

module.exports = ResetTokensTableSchema
