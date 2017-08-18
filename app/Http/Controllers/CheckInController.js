'use strict'

const CheckIn = use('App/Model/CheckIn')
const Venue = use('App/Model/Venue')
const Validator = use('Validator')

class CheckInController {
  * index (request, response) {
    const authUser = request.authUser

    let page = Number(request.input('page', 1))
    let perPage = Number(request.input('per_page', 10))

    if (isNaN(page) || page < 1) {
      page = 1
    }

    if (isNaN(perPage) || perPage < 1) {
      perPage = 10
    }

    const checkIns = yield CheckIn.query()
      .with('venue')
      .where('username', authUser.username)
      .orderBy('created_at', 'desc')
      .paginate(page, perPage)

    response.ok(checkIns)
  }

  * indexForVenue (request, response) {
    const venue = yield Venue.findOrFail(request.param('venue_id'))

    let page = Number(request.input('page', 1))
    let perPage = Number(request.input('per_page', 10))

    if (isNaN(page) || page < 1) {
      page = 1
    }

    if (isNaN(perPage) || perPage < 1) {
      perPage = 10
    }

    const checkIns = yield CheckIn.query()
      .with('user')
      .where('venue_id', venue.id)
      .orderBy('created_at', 'desc')
      .paginate(page, perPage)

    response.ok(checkIns)
  }

  * store (request, response) {
    const authUser = request.authUser
    const venue = yield Venue.findOrFail(request.param('venue_id'))
    const rating = request.input('rating')

    const validation = yield Validator.validate({rating}, {
      rating: 'integer|range:-1,3' // exclusive bounds
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const checkIn = yield CheckIn.create({
      venue_id: venue.id,
      username: authUser.username,
      rating
    })

    yield checkIn.related('user', 'venue').load()

    response.ok(checkIn)
  }

  * update (request, response) {
    const checkIn = yield CheckIn.findOrFail(request.param('id'))
    const authUser = request.authUser

    const rating = request.input('rating')

    if (authUser.username !== checkIn.username) {
      response.unauthorized({message: 'Not allowed to edit other check-ins'})
      return
    }

    const validation = yield Validator.validate({rating}, {
      rating: 'integer|range:-1,3' // exclusive bounds
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    checkIn.rating = rating
    yield checkIn.save()

    yield checkIn.related('user', 'venue').load()

    response.ok(checkIn)
  }

  * destroy (request, response) {
    const checkIn = yield CheckIn.findOrFail(request.param('id'))
    const authUser = request.authUser

    if (authUser.username !== checkIn.username) {
      response.unauthorized({message: 'Not allowed to edit other check-ins'})
      return
    }

    yield checkIn.delete()
    response.ok({
      message: 'Check-in successfully deleted.'
    })
  }
}

module.exports = CheckInController
