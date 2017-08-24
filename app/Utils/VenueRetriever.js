'use strict'

const Config = use('Config')
const Database = use('Database')
const Moment = require('moment')
const Photo = use('App/Model/Photo')
const Request = require('request-promise-native')
const Venue = use('App/Model/Venue')
const VenueCategory = use('App/Model/VenueCategory')
const VenueHoursRange = use('App/Model/VenueHoursRange')
const VenueRetrieval = use('App/Model/VenueRetrieval')

class VenueRetriever {
  /**
   * Fetches the top results for the given parameters from Foursquare and save them in our database.
   *
   * @param lat the latitude
   * @param lng the longitude
   * @param radius the radius in km
   * @param details a boolean to determine if the details should also be fetched
   */
  * retrieve (lat, lng, radius, section) {
    if (lat === null || lng === null || radius === null) {
      return
    }

    // Round the paramters to get "cache hits"
    lat = Math.round(lat * 100) / 100
    lng = Math.round(lng * 100) / 100
    radius = this.roundRadius(radius)

    const oldRetrievalCountResult = yield VenueRetrieval.query().where('lat', lat).where('lng', lng).where('radius', radius).where('section', section).count()
    if (Number(oldRetrievalCountResult[0].count) > 0) {
      return
    }

    const response = yield this.requestFoursquare(lat, lng, radius, section)
    yield this.processFoursquareData(response, section)

    yield VenueRetrieval.create({
      lat, lng, radius, section
    })
  }

  /**
   * Round the given radius to the next power of two.
   * @param radius the radiuss
   * @returns {number} the power of two
   */
  roundRadius (radius) {
    let exp = Math.ceil(Math.log2(radius))
    // The greatest radius that Foursquare supports is 100km, the minimum radius is 1km
    return Math.max(Math.min(Math.pow(2, exp), 100), 1)
  }

  /**
   * Fetch the top results from Foursquare for the given parameters.
   *
   * @param lat the latitude
   * @param lng the longitude
   * @param radius the radius in km
   * @returns {Promise.<TResult>}
   */
  * requestFoursquare (lat, lng, radius, section) {
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
        day: 'any',
        section: section,
        limit: 50,
        venuePhotos: true
      }
    }
    return Request(options)
      .then(response => {
        // console.log(response.response.groups[0].items[0].featuredPhotos)
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
  * processFoursquareData (data, section) {
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

      yield Venue.findOrCreate({
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

      if (section) {
        // Save the venue -> section mapping
        yield Database.raw(`INSERT INTO venue_section (venue_id, section) VALUES (:id, :section) ON CONFLICT DO NOTHING`, {
          id: venue.id,
          section
        })
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
        return acc.concat(this.parseTimeframe(item))
      }, [])

      for (let item of hours) {
        try {
          yield Database.raw('INSERT INTO venue_hours_ranges(venue_id, hours) SELECT :id, f_venue_hours(:start, :end)', {
            id: venue.id,
            start: item[0],
            end: item[1]
          })
        } catch (e) {
          console.warn(e)
        }
      }
    }

    venue.phone = details.contact.formattedPhone

    if (response.response.venue.photos && response.response.venue.photos.groups[0] && response.response.venue.photos.groups[0].items) {
      const photoUrls = response.response.venue.photos.groups[0].items.map(photo => {
        return {
          url: photo.prefix + 'original' + photo.suffix,
          venue_id: venue.id
        }
      })
      yield Photo.createMany(photoUrls)
    }

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
  parseTimeframe (timeframe) {
    const result = []

    // Determine the day of the week of this timeframe
    const dayOffsets = this.parseDays(timeframe.days)
    const days = dayOffsets.map(offset => Moment('1996-01-01').add(offset, 'days'))

    // Determine the time ranges of this timeframe
    let timeRanges
    if (timeframe.open[0].renderedTime.trim() === '24 Hours') {
      timeRanges = result.push(['1996-01-01 00:00', '1996-01-07 23:59'])
    } else {
      timeRanges = timeframe.open.map(item => {
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

          // Catch the dammit entries like Mon-Tue 7:00 pm-1:00 am
          if (endHour < startHour) {
            const nextDay = Moment(day).add(1, 'days')
            result.push([addAndFormat(day, startHour, startMinute), addAndFormat(day, 23, 59)])
            result.push([addAndFormat(nextDay, 0, 0), addAndFormat(nextDay, endHour, endMinute)])
          } else {
            result.push([addAndFormat(day, startHour, startMinute), addAndFormat(day, endHour, endMinute)])
          }
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
