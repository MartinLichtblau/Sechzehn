'use strict'

const Lucid = use('Lucid')

class VenueHoursRange extends Lucid {
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
   * Get the offset in days starting from Monday for the given day of the week abbreviation.
   * @param day the day of the week abbreviation
   * @returns {number}
   */
  static getDayOffset (day) {
    const dayOfTheWeekAbbreviations = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

    return dayOfTheWeekAbbreviations.indexOf(day.trim())
  }
}

module.exports = VenueHoursRange
