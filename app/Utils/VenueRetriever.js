'use strict'

const Url = require('url')
const Database = use('Database')
const Venue = use('App/Model/Venue')
const VenueCategory = use('App/Model/VenueCategory')
const VenueRetrieval = use('App/Model/VenueRetrieval')
const Config = use('Config')
const Request = require('request-promise-native')

class VenueRetriever {
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

  * requestFoursquare (lat, lng, radius) {
    const options = {
      method: 'GET',
      uri: 'https://api.foursquare.com/v2/venues/explore',
      json: true,
      qs: {
        client_id: Config.get('external.foursquare.id'),
        client_secret: Config.get('external.foursquare.secret'),
        v: '20170809',
        m: 'foursquare',
        locale: 'en',
        ll: lat + ',' + lng,
        radius: radius * 1000
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

      yield Venue.findOrCreate({
        id: venue.id
      }, {
        id: venue.id,
        name: venue.name,
        lat: venue.location.lat,
        lng: venue.location.lng,
        address: venue.location.formattedAddress.join('; '),
        url: venue.url,
        foursquare_rating: venue.rating,
        foursquare_rating_count: venue.ratingSignals,
        category: category.id
      })
    }
  }
}

module.exports = VenueRetriever
