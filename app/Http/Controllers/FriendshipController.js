'use strict'

const User = use('App/Model/User')
const Friendship = use('App/Model/Friendship')
const Validator = use('Validator')
const Exceptions = require('adonis-lucid/src/Exceptions')

class FriendshipController {
  * index (request, response) {
    const user = yield User.findOrFail(request.param('id', null))
    const page = Number(request.input('page', 1))
    const perPage = Number(request.input('per_page', 10))

    const validation = yield Validator.validate({page, perPage}, {
      page: 'integer|min:1',
      perPage: 'integer|min:1'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const friends = yield user.friends().where('friendships.status', 'CONFIRMED').paginate(page, perPage)
    response.ok(friends)
  }

  * requests (request, response) {
    const user = yield User.findOrFail(request.param('id', null))
    const page = Number(request.input('page', 1))
    const perPage = Number(request.input('per_page', 10))
    const onlyIncoming = Validator.sanitizor.toBoolean(request.input('only_incoming'))

    if (request.authUser.username !== user.username) {
      response.unauthorized({message: 'Not allowed to show friend requests for other users'})
      return
    }

    const validation = yield Validator.validate({page, perPage}, {
      page: 'integer|min:1',
      perPage: 'integer|min:1'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const query = Friendship.query().where('relating_user', user.username)

    if (onlyIncoming) {
      query.where('friendships.status', 'RELATING_CONFIRMED')
    } else {
      query.whereNot('friendships.status', 'CONFIRMED')
    }

    const friendshipRequests = yield query.with('related_user').paginate(page, perPage)

    response.ok(friendshipRequests)
  }

  * store (request, response) {
    const me = request.authUser
    const other = yield User.findOrFail(request.param('id', null))

    if (me.username === other.username) {
      response.forbidden({
        'message': 'You can not be friends with yourself'
      })
      return
    }

    const existingFriendship = yield Friendship.query().where('relating_user', me.username).where('related_user', other.username).first()

    if (existingFriendship !== null) {
      response.conflict({message: 'Friendship exists or friend request already raised'})
      return
    }

    // I am friends with the other one
    const iAmFriendsWithOther = yield Friendship.create({
      relating_user: me.username,
      related_user: other.username,
      status: 'RELATING_CONFIRMED'
    })

    // The other one may be friends with me
    yield Friendship.create({
      relating_user: other.username,
      related_user: me.username,
      status: 'RELATED_CONFIRMED'
    })

    response.ok({
      relating_user: iAmFriendsWithOther.relating_user,
      status: iAmFriendsWithOther.status,
      related_user: other
    })
  }

  * update (request, response) {
    const me = request.authUser
    const other = yield User.findOrFail(request.param('id', null))
    const statusData = request.only('status')

    const validation = yield Validator.validate(statusData, {
      status: 'string|in:CONFIRMED,DECLINED'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const iAmFriendsWithOther = yield Friendship.query().where('relating_user', me.username).where('related_user', other.username).first()
    const otherIsFriendsWithMe = yield Friendship.query().where('relating_user', other.username).where('related_user', me.username).first()

    if (iAmFriendsWithOther === null || otherIsFriendsWithMe === null) {
      throw new Exceptions.ModelNotFoundException()
    }

    if (iAmFriendsWithOther.status === 'CONFIRMED' && otherIsFriendsWithMe.status === 'CONFIRMED') {
      response.forbidden({
        message: 'Friendship already confirmed'
      })
      return
    }

    if (statusData.status === 'DECLINED') {
      yield iAmFriendsWithOther.delete()
      yield otherIsFriendsWithMe.delete()

      response.ok({
        relating_user: me.username,
        status: 'DECLINED',
        related_user: other
      })
      return
    }

    // If request should be accepted

    if (iAmFriendsWithOther.status === 'RELATING_CONFIRMED' && otherIsFriendsWithMe.status === 'RELATED_CONFIRMED') {
      response.forbidden({
        message: `Friendship must be confirmed from ${other.username}`
      })
    } else if (iAmFriendsWithOther.status === 'RELATED_CONFIRMED' && otherIsFriendsWithMe.status === 'RELATING_CONFIRMED') {
      iAmFriendsWithOther.status = 'CONFIRMED'
      otherIsFriendsWithMe.status = 'CONFIRMED'

      yield iAmFriendsWithOther.save()
      yield otherIsFriendsWithMe.save()

      response.ok({
        relating_user: iAmFriendsWithOther.relating_user,
        status: iAmFriendsWithOther.status,
        related_user: other
      })
    } else {
      response.internalServerError()
    }
  }

  * destroy (request, response) {
    const me = request.authUser
    const other = yield User.findOrFail(request.param('id', null))

    const iAmFriendsWithOther = yield Friendship.query().where('relating_user', me.username).where('related_user', other.username).first()
    const otherIsFriendsWithMe = yield Friendship.query().where('relating_user', other.username).where('related_user', me.username).first()

    if (iAmFriendsWithOther !== null && otherIsFriendsWithMe !== null) {
      yield iAmFriendsWithOther.delete()
      yield otherIsFriendsWithMe.delete()

      response.ok({
        message: 'Friendship deleted'
      })
    } else {
      throw new Exceptions.ModelNotFoundException()
    }
  }
}

module.exports = FriendshipController
