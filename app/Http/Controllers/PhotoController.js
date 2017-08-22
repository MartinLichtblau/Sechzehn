'use strict'

const Comment = use('App/Model/Comment')
const Photo = use('App/Model/Photo')
const Storage = use('Storage')
const User = use('App/Model/User')
const Venue = use('App/Model/Venue')

class PhotoController {
  * indexForUser (request, response) {
    const user = yield User.findOrFail(request.param('username'))

    let page = Number(request.input('page', 1))
    let perPage = Number(request.input('per_page', 10))

    if (isNaN(page) || page < 1) {
      page = 1
    }

    if (isNaN(perPage) || perPage < 1) {
      perPage = 10
    }

    const photos = yield Photo.query()
      .with('venue')
      .scope('venue', builder => {
        builder.leftOuterJoin(Venue.ratingQuery, 'rating_query.venue_id', 'venues.id')
      })
      .where('username', user.username)
      .orderBy('created_at', 'desc')
      .paginate(page, perPage)

    response.ok(photos)
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

    const photos = yield Photo.query()
      .with('user')
      .where('venue_id', venue.id)
      .orderBy('created_at', 'desc')
      .paginate(page, perPage)

    response.ok(photos)
  }

  * store (request, response) {
    const venue = yield Venue.findOrFail(request.param('venue_id'))
    const authUser = request.authUser

    const file = request.file('photo', {
      maxSize: '500kb',
      allowedExtensions: ['jpg', 'png', 'jpeg', 'JPG', 'PNG', 'JPEG']
    })

    const url = yield Storage.store(file)

    const photo = yield Photo.create({
      username: authUser.username,
      venue_id: venue.id,
      url
    })

    yield photo
      .related('venue')
      .scope('venue', builder => {
        builder.leftOuterJoin(Venue.ratingQuery, 'rating_query.venue_id', 'venues.id')
      })
      .load()

    response.created(photo)
  }

  * show (request, response) {
    const photo = yield Photo.findOrFail(request.param('id'))

    yield photo
      .related('user', 'venue')
      .scope('venue', builder => {
        builder.leftOuterJoin(Venue.ratingQuery, 'rating_query.venue_id', 'venues.id')
      })
      .load()

    if (request.authUser.username === photo.username) {
      photo.user.isOwner = true
    }

    response.ok(photo)
  }

  * destroy (request, response) {
    const photo = yield Photo.findOrFail(request.param('id'))

    if (request.authUser.username !== photo.username) {
      response.unauthorized({message: 'Not allowed to edit other photos than yours.'})
      return
    }

    yield Comment.query().where('photo_id', photo.id).update('photo_id', null)

    const oldUrl = photo.url
    // Delete the old picture
    yield Storage.delete(oldUrl)

    yield photo.delete()
    response.ok({
      message: 'Photo successfully deleted.'
    })
  }
}

module.exports = PhotoController
