'use strict'

const Venue = use('App/Model/Venue')
const VenueCategory = use('App/Model/VenueCategory')

class VenueCategoryController {
  * index (request, response) {
    //
  }

  * show (request, response) {
    const category = VenueCategory.findOrFail(request.param('id', null)).with()
  }

  * store (request, response) {
    //
  }

  * update (request, response) {
    //
  }

  * destroy (request, response) {
    //
  }
}

module.exports = VenueCategoryController
