'use strict'

const Url = require('url')
const Database = use('Database')
const Venue = use('App/Model/Venue')
const VenueRetrieval = use('App/Model/VenueRetrieval')
const Route = use('Route')
const Config = use('Config')
const Http = require('http')

class VenueRetriever {
  * retrieve (lat, lng, radius) {
    const roundedLat = Math.round(lat * 10) / 10
    const roundedLng = Math.round(lng * 10) / 10

    const roundedRadius = this.roundRadius(radius)

    const oldRetrievalCount = yield VenueRetrieval.query().where('lat', roundedLat).where('lng', roundedLng).where('radius', roundedRadius).count()

    /*
        yield VenueRetrieval.create({
          lat: roundedLat,
          lng: roundedLng,
          radius: roundedRadius
        })
    */

    console.log(roundedRadius)
  }

  /**
   * Round the given radius to the next power of two.
   * @param radius the radiuss
   * @returns {number} the power of two
   */
  roundRadius (radius) {
    let exp = Math.ceil(Math.log2(radius))
    return Math.pow(2, exp)
  }

  * requestFoursquare (lat, lng, radius) {
    return Http.get({
      host: 'https://api.foursquare.com',
      path: '/v2/venues/search'
    }, function (response) {
      // Continuously update stream with data
      let body = ''
      response.on('data', d => {
        body += d
      })
      response.on('end', () => {
        this.processFoursquareData(JSON.parse(body))
      })
    })
  }

  * processFoursquareData (data) {
    console.log(data)
  }
}

module.exports = VenueRetriever
