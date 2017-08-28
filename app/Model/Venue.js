'use strict'

const Lucid = use('Lucid')
const Database = use('Database')
const Validator = use('Validator')

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
      'price'
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
      'url',
      'phone',
      'address',
      'description',
      'rating',
      'rating_count',
      'top_visitors',
      'checkins_count'
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
  checkins () {
    return this.hasMany('App/Model/CheckIn')
  }

  /**
   * The Comments to this Venue.
   * @returns {Object}
   */
  comments () {
    return this.hasMany('App/Model/Comment')
  }

  /**
   * The Photos of this Venue.
   * @returns {Object}
   */
  photos () {
    return this.hasMany('App/Model/Photo')
  }

  /**
   * The computed properties.
   */
  static get computed () {
    return ['rating', 'rating_count']
  }

  /**
   * Determine the rating as the average of the foursquare rating and the CheckIns ratings.
   * @returns {number}
   */
  getRating () {
    let result = -1

    const checkinsRating = Number(this.checkins_rating)
    const foursquareRating = Number(this.foursquare_rating)

    const checkinsRatingCount = Number(this.checkins_rating_count)
    const foursquareRatingCount = Number(this.foursquare_rating_count)

    const checkinsRatingExists = !isNaN(checkinsRating) && !isNaN(checkinsRatingCount) && checkinsRatingCount > 0
    const foursquareRatingExists = !isNaN(foursquareRating) && !isNaN(foursquareRatingCount) && foursquareRatingCount > 0

    if (checkinsRatingExists && foursquareRatingExists) {
      result = (checkinsRating + foursquareRating) / 2
    } else if (checkinsRatingExists && !foursquareRatingExists) {
      result = checkinsRating
    } else if (!checkinsRatingExists && foursquareRatingExists) {
      result = foursquareRating
    }

    return Math.round(result * 100) / 100
  }

  /**
   * Calculate the count of ratings as the count of the foursquare ratings plus the count of CheckIns.
   * @returns {number}
   */
  getRatingCount () {
    const checkinsRatingCount = Number(this.checkins_rating_count)
    const foursquareRatingCount = Number(this.foursquare_rating_count)

    return (!isNaN(checkinsRatingCount) ? checkinsRatingCount : 0) + (!isNaN(foursquareRatingCount) ? foursquareRatingCount : 0)
  }

  /**
   * Returns the subquery for retrieving the ratings from the CheckIns.
   * @returns {Object|*}
   */
  static get ratingQuery () {
    return Database.select('venue_id', Database.raw('avg(check_ins.rating) * 5 as checkins_rating'), Database.raw('count(check_ins.rating) as checkins_rating_count')).from('check_ins').groupBy('venue_id').as('rating_query')
  }

  /**
   * Cast the checkins_count into a Number.
   * @param count
   * @returns {number}
   */
  getCheckinsCount (count) {
    return count ? Number(count) : count
  }
}

module.exports = Venue
