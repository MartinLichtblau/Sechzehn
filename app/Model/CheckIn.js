'use strict'

const Lucid = use('Lucid')

class CheckIn extends Lucid {
  static get hidden () {
    return [
      'updated_at',
      'venue_id',
      'username'
    ]
  }

  user () {
    return this.belongsTo('App/Model/User', 'username', 'username')
  }

  venue () {
    return this.belongsTo('App/Model/Venue')
  }

}

module.exports = CheckIn
