'use strict'

const Comment = use('App/Model/Comment')
const Photo = use('App/Model/Photo')
const Storage = use('Storage')
const User = use('App/Model/User')
const Venue = use('App/Model/Venue')
const Validator = use('Validator')

class CommentController {
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

    const comments = yield Comment.query()
      .with('venue', 'photo')
      .scope('venue', builder => {
        builder.leftOuterJoin(Venue.ratingQuery, 'rating_query.venue_id', 'venues.id')
      })
      .where('username', user.username)
      .orderBy('created_at', 'desc')
      .paginate(page, perPage)

    response.ok(comments)
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

    const comments = yield Comment.query()
      .with('user', 'photo')
      .where('venue_id', venue.id)
      .orderBy('created_at', 'desc')
      .paginate(page, perPage)

    response.ok(comments)
  }

  * store (request, response) {
    const venue = yield Venue.findOrFail(request.param('venue_id'))
    const authUser = request.authUser

    const body = request.input('body')

    const validation = yield Validator.validate({body}, {
      body: 'required'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const photo = yield Photo.find(request.input('photo_id'))

    if (photo) {
      if (request.authUser.username !== photo.username) {
        response.unauthorized({message: 'Not allowed to use other photos than yours.'})
        return
      }
    }

    const comment = yield Comment.create({
      username: authUser.username,
      venue_id: venue.id,
      body: body,
      photo_id: photo ? photo.id : null
    })

    yield comment.related('photo').load()

    response.created(comment)
  }

  * update (request, response) {
    const comment = yield Comment.findOrFail(request.param('id'))

    if (request.authUser.username !== comment.username) {
      response.unauthorized({message: 'Not allowed to edit other comments than yours.'})
      return
    }

    const body = request.input('body')

    const validation = yield Validator.validate({body}, {
      body: 'required'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const photo = yield Photo.find(request.input('photo_id'))

    if (photo) {
      if (request.authUser.username !== photo.username) {
        response.unauthorized({message: 'Not allowed to use other photos than yours.'})
        return
      }
      comment.photo_id = photo.id
    } else {
      comment.photo_id = null
    }

    comment.body = body

    yield comment.save()

    yield comment.related('photo').load()

    response.ok(comment)
  }

  * destroy (request, response) {
    const comment = yield Comment.findOrFail(request.param('id'))

    if (request.authUser.username !== comment.username) {
      response.unauthorized({message: 'Not allowed to edit other comments than yours.'})
      return
    }

    yield comment.delete()

    response.ok({
      message: 'Comment successfully deleted.'
    })
  }
}

module.exports = CommentController
