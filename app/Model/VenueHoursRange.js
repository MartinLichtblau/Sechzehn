'use strict'

const Lucid = use('Lucid')

class VenueHoursRange extends Lucid {
  /**
   * The fields which are visible per default for this Model (i.e for JSON serialization).
   *
   * @returns {string[]}
   */

  static get visible () {
    return [
      'day',
      'start',
      'end'
    ]
  }

  static get computed () {
    return ['day', 'start', 'end']
  }

  getDay () {
    const day = Number(this.hours.substring(11, 12)) - 1
    return VenueHoursRange.dayOfTheWeekAbbreviations[day]
  }

  getStart () {
    return this.hours.substring(13, 18)
  }

  getEnd () {
    return this.hours.substring(35, 40)
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

  static get dayOfTheWeekAbbreviations () {
    return ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
  }

  /**
   * Get the offset in days starting from Monday for the given day of the week abbreviation.
   * @param day the day of the week abbreviation
   * @returns {number}
   */
  static getDayOffset (day) {
    return VenueHoursRange.dayOfTheWeekAbbreviations.indexOf(day.trim())
  }
}

module.exports = VenueHoursRange
