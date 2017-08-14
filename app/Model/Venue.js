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
      'id',
      'name',
      'lat',
      'lng',
      'price',
      'foursquare_rating'
    ]
  }

  static get detailed () {
    return [
      'id',
      'name',
      'lat',
      'lng',
      'price',
      'foursquare_rating',
      'url',
      'phone',
      'address',
      'description'
    ]
  }

  detailView () {
    return {
      id: this.id,
      name: this.name,
      lat: this.lat,
      lng: this.lng,
      price: this.price,
      url: this.url,
      phone: this.phone,
      address: this.address,
      description: this.description
    }
  }

  category () {
    return this.belongsTo('App/Model/VenueCategory', 'id', 'category_id')
  }
}

module.exports = Venue
