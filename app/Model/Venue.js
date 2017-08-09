'use strict'

const Lucid = use('Lucid')

class Venue extends Lucid {
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
      'category'
    ]
  }

  category () {
    return this.belongsTo('App/Model/VenueCategory', 'category', 'id')
  }
}

module.exports = Venue
