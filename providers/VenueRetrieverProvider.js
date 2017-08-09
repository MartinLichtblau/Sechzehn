'use strict'

const ServiceProvider = require('adonis-fold').ServiceProvider

class VenueRetrieverProvider extends ServiceProvider {
  * register () {
    this.app.singleton('App/Utils/VenueRetriever', function (app) {
      const VenueRetriever = require('../app/Utils/VenueRetriever')
      return new VenueRetriever()
    })
  }
}

module.exports = VenueRetrieverProvider
