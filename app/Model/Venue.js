'use strict'

const Lucid = use('Lucid')

class Venue extends Lucid {
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
   * The fields which are visible per default for this Model (i.e for JSON serialization).
   *
   * @returns {string[]}
   */
  static get visible () {
    return [
      'name',
      'lat',
      'lng',
      'price',
      'category',
      'foursquare_rating'
    ]
  }

  category () {
    return this.belongsTo('App/Model/VenueCategory', 'category', 'id')
  }
}

module.exports = Venue
