'use strict'

const Lucid = use('Lucid')

class ResetToken extends Lucid {
  /**
   * Set the primary key for this Model to token.
   * @returns {string}
   */
  static get primaryKey () {
    return 'token'
  }

  /**
   * Disable updateTimestamp.
   * @returns {null}
   */
  static get updateTimestamp () {
    return null
  }

  /**
   * The primary key can not be incremented.
   * @returns {boolean}
   */
  static get incrementing () {
    return false
  }

  /**
   * Get the user that is related to this ResetToken.
   * @returns {Object}
   */
  // user () {
  // return this.belongsTo('App/Model/User', 'username', 'user')
  // return this.belongsTo('App/Model/User', 'user', 'username')
  // }
}

module.exports = ResetToken
