'use strict'

const Schema = use('Schema')

class CheckInsTableSchema extends Schema {
  up () {
    this.create('check_ins', (table) => {
      table.increments()
      table.timestamps()
      table.string('venue_id', 60).notNullable()
      table.string('username', 80).notNullable()
      table.specificType('rating', 'smallint')
    })
  }

  down () {
    this.drop('check_ins')
  }
}

module.exports = CheckInsTableSchema
