'use strict'

const Lucid = use('Lucid')

class VenueCategory extends Lucid {
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

  venues () {
    return this.hasMany('App/Model/Venue')
  }
}

module.exports = VenueCategory