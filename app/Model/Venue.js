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
  static get visibleList () {
    return [
      'venues.id',
      'venues.name',
      'lat',
      'lng',
      'price',
      // 'photo',
      'foursquare_rating'
    ]
  }

  /**
   * The fields which are visible per default for this Model (i.e for JSON serialization).
   *
   * @returns {string[]}
   */
  static get visible () {
    return [
      'id',
      'name',
      'lat',
      'lng',
      'price',
      'foursquare_rating',
      'url',
      // 'photo',
      'phone',
      'address',
      'description'
    ]
  }

  /**
   * The related Category.
   * @returns {Category}
   */
  category () {
    return this.belongsTo('App/Model/VenueCategory', 'id', 'category_id')
  }

  /**
   * The related Opening Hours.
   * @returns {Object}
   */
  hours () {
    return this.hasMany('App/Model/VenueHoursRange')
  }

  /**
   * The Check-Ins for this Venue.
   * @returns {Object}
   */
  checkIns () {
    return this.hasMany('App/Model/CheckIn')
  }
}

module.exports = Venue
