'use strict'

const Lucid = use('Lucid')

class Photo extends Lucid {
  static get hidden () {
    return [
      'updated_at',
      'venue_id',
      'username'
    ]
  }
  venue () {
    return this.belongsTo('App/Model/Venue')
  }

  user () {
    return this.belongsTo('App/Model/User', 'username', 'username')
  }
}

module.exports = Photo
