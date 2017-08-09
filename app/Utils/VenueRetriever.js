'use strict'

const Url = require('url')
const Database = use('Database')
const Venue = use('App/Model/Venue')
const VenueRetrieval = use('App/Model/VenueRetrieval')
const Route = use('Route')
const Config = use('Config')

class VenueRetriever {
  * retrieve (lat, lng, radius) {
    const roundedLat = Math.round(lat * 10) / 10
    const roundedLng = Math.round(lng * 10) / 10

    const roundedRadius = this.roundRadius(radius)

    console.log(radius)
  }

  roundRadius (radius) {
    let exp = Math.round(Math.sqrt(radius))

    return Math.pow(2, exp)
  }
}

module.exports = VenueRetriever
