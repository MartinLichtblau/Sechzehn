'use strict'

const Venue = use('App/Model/Venue')
const VenueCategory = use('App/Model/VenueCategory')
const VenueHoursRange = use('App/Model/VenueHoursRange')
const VenueRetrieval = use('App/Model/VenueRetrieval')
const Config = use('Config')
const Request = require('request-promise-native')
const Moment = require('moment')

class VenueRetriever {
  /**
   * Fetches the top results for the given parameters from Foursquare and save them in our database.
   *
   * @param lat the latitude
   * @param lng the longitude
   * @param radius the radius in km
   */
  * retrieve (lat, lng, radius) {
    if (lat === null || lng === null || radius === null) {
      return
    }

    // Round the paramters to get "cache hits"
    lat = Math.round(lat * 10) / 10
    lng = Math.round(lng * 10) / 10
    radius = this.roundRadius(radius)

    const oldRetrievalCountResult = yield VenueRetrieval.query().where('lat', lat).where('lng', lng).where('radius', radius).count()
    if (Number(oldRetrievalCountResult[0].count) > 0) {
      return
    }

    const response = yield this.requestFoursquare(lat, lng, radius)
    yield this.processFoursquareData(response)

    yield VenueRetrieval.create({
      lat, lng, radius
    })
  }

  /**
   * Round the given radius to the next power of two.
   * @param radius the radiuss
   * @returns {number} the power of two
   */
  roundRadius (radius) {
    let exp = Math.ceil(Math.log2(radius))
    // The greatest radius that Foursquare supports is 100km
    return Math.min(Math.pow(2, exp), 100)
  }

  /**
   * Fetch the top results from Foursquare for the given parameters.
   *
   * @param lat the latitude
   * @param lng the longitude
   * @param radius the radius in km
   * @returns {Promise.<TResult>}
   */
  * requestFoursquare (lat, lng, radius) {
    const options = {
      method: 'GET',
      uri: 'https://api.foursquare.com/v2/venues/explore',
      json: true,
      qs: {
        client_id: Config.get('external.foursquare.id'),
        client_secret: Config.get('external.foursquare.secret'),
        v: '20170814',
        m: 'swarm',
        locale: 'en',
        ll: lat + ',' + lng,
        radius: radius * 1000,
        time: 'any',
        day: 'any'
      }
    }
    return Request(options)
      .then(response => {
        return response
      })
      .catch(error => {
        console.warn(error)
      })
  }

  /**
   * Convert and save the data from Foursquare in our database.
   *
   * @param data
   */
  * processFoursquareData (data) {
    const venues = data.response.groups.reduce((acc, group) => acc.concat(group.items.map(item => item.venue)), [])

    for (let venue of venues) {
      let category = venue.categories.find(cat => cat.primary)

      if (category) {
        category = yield VenueCategory.findOrCreate({
          id: category.id
        }, {
          id: category.id,
          plural_name: category.pluralName,
          short_name: category.shortName,
          name: category.name,
          icon: category.icon.prefix + '64' + category.icon.suffix
        })
      }

      const venueFromDb = yield Venue.findOrCreate({
        id: venue.id
      }, {
        id: venue.id,
        name: venue.name,
        lat: venue.location.lat,
        lng: venue.location.lng,
        address: venue.location.formattedAddress.join('; '),
        price: venue.price ? venue.price.tier : null,
        url: venue.url,
        foursquare_rating: venue.rating,
        foursquare_rating_count: venue.ratingSignals,
        category_id: category.id
      })

      if (!venueFromDb.details_fetched) {
        yield this.retrieveDetails(venueFromDb)
      }
    }
  }

  /**
   * Enrich the given Venue with data from Foursquare.
   *
   * @param venue
   */
  * retrieveDetails (venue) {
    const response = yield this.requestFoursquareDetails(venue.id)
    const details = response.response.venue

    // Save the opening hours
    if (details.hours && details.hours.timeframes) {
      const hours = details.hours.timeframes.reduce((acc, item) => {
        return acc.concat(this.processTimeframe(item))
      }, [])

      const hoursRanges = hours.map(item => {
        return {
          venue_id: venue.id,
          hours: item
        }
      })

      yield VenueHoursRange.createMany(hoursRanges)
    }

    // TODO: save Comments and Photos

    venue.phone = details.contact.formattedPhone

    venue.details_fetched = true

    yield venue.save()
  }

  /**
   * Fetches the detailed information for the given venue id.
   *
   * @param venueId the ID of the Venue
   * @returns {Promise.<TResult>}
   */
  * requestFoursquareDetails (venueId) {
    const options = {
      method: 'GET',
      uri: 'https://api.foursquare.com/v2/venues/' + venueId,
      json: true,
      qs: {
        client_id: Config.get('external.foursquare.id'),
        client_secret: Config.get('external.foursquare.secret'),
        v: '20170809',
        m: 'foursquare',
        locale: 'en'
      }
    }
    return Request(options)
      .then(response => {
        return response
      })
      .catch(error => {
        console.warn(error)
      })
  }

  /**
   * Converts one timeframe from the Hours Response (https://developer.foursquare.com/docs/responses/hours)
   * into the time ranges that are needed for our database.
   *
   * @param timeframe
   * @returns {Array}
   */
  processTimeframe (timeframe) {
    const result = []

    // Determine the day of the week of this timeframe
    const dayOffsets = this.parseDays(timeframe.days)
    const days = dayOffsets.map(offset => Moment('1996-01-01').add(offset, 'days'))

    // Determine the time ranges of this timeframe
    const timeRanges = timeframe.open.map(item => {
      return item.renderedTime.split('–').map(time => {
        if (time.trim() === 'Midnight') {
          time = '11:59 pm'
        }
        if (time.trim() === 'Noon') {
          time = '12:00 pm'
        }
        return Moment(time, 'h:m a')
      })
    })

    const addAndFormat = (day, hours, minutes) => Moment(day).add(hours, 'hours').add(minutes, 'minutes').format('YYYY-MM-DD HH:mm')

    // Combine both
    for (let day of days) {
      for (let timeRange of timeRanges) {
        let startHour = timeRange[0].hours()
        let startMinute = timeRange[0].minutes()
        let endHour = timeRange[1].hours()
        let endMinute = timeRange[1].minutes()

        // Catch the day wrap
        if (startHour > endHour) {
          const nextDay = Moment('1996-01-01').add((day.day() + 1) % 7, 'days')

          result.push('[' + addAndFormat(day, startHour, startMinute) + ',' + addAndFormat(day, 23, 59) + ']')
          result.push('[' + addAndFormat(nextDay, 0, 0) + ',' + addAndFormat(nextDay, endHour, endMinute) + ']')
        } else {
          result.push('[' + addAndFormat(day, startHour, startMinute) + ',' + addAndFormat(day, endHour, endMinute) + ']')
        }
      }
    }

    return result
  }

  /**
   * Converts a string like 'Mon-Fri, Sun' into an array of offsets from Monday (i.e. [0,1,2,3,4,6]).
   *
   * @param days the days string
   * @returns {Array} the array of day offsets
   */
  parseDays (days) {
    if (!days) {
      return []
    }

    const result = []

    for (let item of days.split(',')) {
      if (item.includes('–')) {
        const dayRange = item.split('–')
        const first = VenueHoursRange.getDayOffset(dayRange[0])
        const last = VenueHoursRange.getDayOffset(dayRange[1])

        for (let i = first; i <= last; i++) {
          result.push(i)
        }
      } else {
        result.push(VenueHoursRange.getDayOffset(item))
      }
    }

    return result
  }
}

module.exports = VenueRetriever
