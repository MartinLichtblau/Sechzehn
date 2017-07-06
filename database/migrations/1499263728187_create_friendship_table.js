'use strict'

const Schema = use('Schema')

class FriendshipsTableSchema extends Schema {
  up () {
    this.raw(`CREATE TYPE e_friendship_status AS ENUM ('RELATING_CONFIRMED', 'RELATED_CONFIRMED', 'CONFIRMED')`)
    this.create('friendships', (table) => {
      table.increments()
      table.string('relating_user', 80)
      table.string('related_user', 80)
      table.specificType('status', 'e_friendship_status')
      table.foreign('relating_user').references('users.username')
      table.foreign('related_user').references('users.username')
    })
  }

  down () {
    this.drop('friendships')
    this.raw('DROP TYPE IF EXISTS e_friendship_status')
  }
}

module.exports = FriendshipsTableSchema
