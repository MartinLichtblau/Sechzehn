'use strict'

const Lucid = use('Lucid')

class Friendship extends Lucid {
  /**
   * The fields which are visible per default for this Model (i.e for JSON serialization).
   *
   * @returns {[string,string,string]}
   */
  static get visible () {
    return [
      'relating_user',
      'status'
    ]
  }

  /**
   * Disable createTimestamp.
   * @returns {null}
   */
  static get createTimestamp () {
    return null
  }

  /**
   * Disable updateTimestamp.
   * @returns {null}
   */
  static get updateTimestamp () {
    return null
  }

  /**
   * Fetch the related_user as User object (and not just the username).
   * @returns {Object}
   */
// eslint-disable-next-line camelcase
  related_user () {
    return this.belongsTo('App/Model/User', 'username', 'related_user')
  }
}

module.exports = Friendship
