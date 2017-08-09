'use strict'

const Url = require('url')
const Database = use('Database')
const Venue = use('App/Model/Venue')
const Route = use('Route')
const Config = use('Config')

class VenueRetriever {
  * retrieve (lat, lng, radius) {
    const roundedLat = Math.round(lat * 10) / 10
    const roundedLng = Math.round(lng * 10) / 10

    console.log('Venue')
  }
}

module.exports = VenueRetriever
