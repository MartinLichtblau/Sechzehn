'use strict'

const Lucid = use('Lucid')

class Comment extends Lucid {
  static get hidden () {
    return [
      'updated_at',
      'venue_id',
      'username',
      'photo_id'
    ]
  }

  venue () {
    return this.belongsTo('App/Model/Venue')
  }

  user () {
    return this.belongsTo('App/Model/User', 'username', 'username')
  }

  photo () {
    return this.belongsTo('App/Model/Photo')
  }
}

module.exports = Comment
