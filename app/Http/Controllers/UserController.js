'use strict'

const Config = use('Config')
const Database = use('Database')
const Exceptions = require('adonis-lucid/src/Exceptions')
const Hash = use('Hash')
const Mail = use('Mail')
const Pagination = require('./Helper/Pagination')
const Path = require('path')
const Route = use('Route')
const Storage = use('Storage')
const TokenGenerator = use('TokenGenerator')
const Url = require('url')
const User = use('App/Model/User')
const Validator = use('Validator')
const Venue = use('App/Model/Venue')

class UserController {
  * index (request, response) {
    const authUsername = request.authUser.username
    const lat = request.input('lat')
    const lng = request.input('lng')
    const radius = request.input('radius', 10)
    const searchQuery = request.input('query')
    let isFriend = request.input('is_friend')

    const pagination = new Pagination(request)

    // The main query
    const query = User.query().column(User.visibleList)

    // The query for calculation the total count
    const totalQuery = User.query()

    // Get the friends list
    const friendsQuery = Database.from('friendships').select('relating_user as username')
      .where('related_user', authUsername).where('friendships.status', 'CONFIRMED')

    if (searchQuery !== null) {
      // The part of the query that is responsible for defining the similarity
      const similarityQueryString = 'GREATEST(similarity(username, :searchQuery), similarity(real_name, :searchQuery))'
      const queryParams = {
        searchQuery: searchQuery,
        threshold: Config.get('app.search.thresholdUser')
      }

      query.select(Database.raw(similarityQueryString + ' as similarity', {searchQuery: searchQuery}))
        .whereRaw(similarityQueryString + ' > :threshold', queryParams)
        .orderBy('similarity', 'DESC')

      totalQuery.whereRaw(similarityQueryString + ' > :threshold', queryParams)
    }

    if (lat !== null && lng !== null) {
      const validation = yield Validator.validate({lat, lng, radius}, {
        lat: 'required|range:-180,180',
        lng: 'required|range:-180,180',
        radius: 'required|range:0,6371'
      })

      if (validation.fails()) {
        response.unprocessableEntity(validation.messages())
        return
      }

      // Calculate the distance between the search point and the user's position
      query.select(Database.raw('(earth_distance(ll_to_earth(lat, lng), ll_to_earth(?, ?)) / 1000) as distance', [lat, lng]))

      // Check if location of the user is in the circle around the search point
      // See https://www.postgresql.org/docs/8.3/static/earthdistance.html
      const inRadiusQuery = 'earth_box(ll_to_earth(?, ?), ?) @> ll_to_earth(lat, lng)'
      query.whereRaw(inRadiusQuery, [lat, lng, radius * 1000])
      query.orderBy('distance', 'asc')

      totalQuery.whereRaw(inRadiusQuery, [lat, lng, radius * 1000])
    }

    // If friend check is enabled
    if (isFriend !== null) {
      isFriend = Validator.sanitizor.toBoolean(isFriend)

      if (isFriend) {
        query.where('username', 'in', friendsQuery)
        totalQuery.where('username', 'in', friendsQuery)
      } else {
        query.whereNot('incognito', true).where('username', 'not in', friendsQuery)
        totalQuery.whereNot('incognito', true).where('username', 'not in', friendsQuery)
      }
    } else {
      query.where(function () {
        this.whereNot('incognito', true).orWhere('username', 'in', friendsQuery)
      })
      totalQuery.where(function () {
        this.whereNot('incognito', true).orWhere('username', 'in', friendsQuery)
      })
    }

    query.orderBy('username', 'asc')

    // Fetch the actual data and complete the Pagination object
    const totalResult = yield totalQuery.count().first()
    pagination.data = yield query.forPage(pagination.page, pagination.perPage)

    pagination.total = Number(totalResult.count)
    response.ok(pagination)
  }

  * store (request, response) {
    const userData = request.only('username', 'email', 'password')
    // Make all usernames lowercase
    userData.username = userData.username.toLowerCase()
    const validation = yield Validator.validate(userData, User.rules)

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const user = yield User.create(userData)

    yield this.generateEmailConfirmation(user)
    yield user.save()

    user.isOwner = true

    response.created(user)
  }

  * show (request, response) {
    const authUsername = request.authUser.username

    let user
    if (request.param('id') === authUsername) {
      user = request.authUser
      user.isOwner = true
    } else {
      user = yield User.query().where('username', request.param('id'))
        .leftJoin(Database.raw('friendships on users.username = friendships.relating_user and ? = friendships.related_user', [authUsername]))
        .first()
    }

    if (!user) {
      throw new Exceptions.ModelNotFoundException()
    }

    if (!user.incognito || user.getFriendshipStatus() === 'CONFIRMED' || user.getFriendshipStatus() === 'SELF') {
      yield user
        .related('photos', 'checkins', 'checkins.venue')
        .scope('photos', builder => {
          builder.orderBy('created_at', 'desc')
          builder.limit(3)
        })
        .scope('checkins', builder => {
          builder.orderBy('created_at', 'desc')
          builder.limit(10)
        })
        .scope('checkins.venue', builder => {
          builder.withCount('checkins')
          builder.columns(Database.raw('venues.*'), 'checkins_rating', 'checkins_rating_count')
          builder.leftOuterJoin(Venue.ratingQuery, 'rating_query.venue_id', 'venues.id')
        })
        .load()
    }
    response.ok(user)
  }

  * update (request, response) {
    const user = yield User.findOrFail(request.param('id'))
    const userData = request.only('real_name', 'city', 'date_of_birth', 'incognito')

    if (request.authUser.username !== user.username) {
      response.unauthorized({message: 'Not allowed to edit other users'})
      return
    }

    const validation = yield Validator.validate(userData, User.rulesForUpdate)

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    user.fill(userData)
    yield user.save()

    user.isOwner = true
    response.ok(user)
  }

  * updateProfilePicture (request, response) {
    const user = yield User.findOrFail(request.param('id'))

    if (request.authUser.username !== user.username) {
      response.unauthorized({message: 'Not allowed to edit other users'})
      return
    }

    const profilePicture = request.file('profile_picture', {
      maxSize: '500kb',
      allowedExtensions: ['jpg', 'png', 'jpeg', 'JPG', 'PNG', 'JPEG']
    })

    const oldUrl = user.profile_picture

    // Save the new image
    user.profile_picture = yield Storage.store(profilePicture)
    yield user.save()

    // Delete the old picture
    yield Storage.delete(oldUrl)

    user.isOwner = true
    response.ok(user)
  }

  * updatePassword (request, response) {
    const user = yield User.findOrFail(request.param('id'))

    if (request.authUser.username !== user.username) {
      response.unauthorized({message: 'Not allowed to edit other users'})
      return
    }

    const userData = request.only('old_password', 'password')
    const validation = yield Validator.validate(userData, {password: 'required|string', old_password: 'required'})

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    try {
      yield request.auth.validate(user.email, userData.old_password)
    } catch (e) {
      response.unprocessableEntity([
        {
          'field': 'old_password',
          'validation': 'password_match',
          'message': 'old_password does not not match'
        }
      ])
      return
    }

    user.password = yield Hash.make(userData.password)
    yield user.save()

    user.isOwner = true
    response.ok(user)
  }

  * updateEmail (request, response) {
    const user = yield User.findOrFail(request.param('id'))

    if (request.authUser.username !== user.username) {
      response.unauthorized({message: 'Not allowed to edit other users'})
      return
    }

    const userData = request.only('password', 'email')
    const validation = yield Validator.validate(userData, {
      password: 'required',
      email: `required|email|unique:users,email,username,${user.username}`
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    try {
      yield request.auth.validate(user.email, userData.password)
    } catch (e) {
      response.unprocessableEntity([
        {
          'field': 'password',
          'validation': 'password_match',
          'message': e.message
        }
      ])
      return
    }

    yield this.generateEmailConfirmation(user)

    user.email = userData.email
    user.confirmed = false

    yield user.save()

    user.isOwner = true
    response.ok(user)
  }

  * updateLocation (request, response) {
    const user = yield User.findOrFail(request.param('id'))

    if (request.authUser.username !== user.username) {
      response.unauthorized({message: 'Not allowed to edit other users'})
      return
    }

    const location = request.only('lat', 'lng')

    const validation = yield Validator.validate(location, {
      lat: 'required|range:-90,90',
      lng: 'required|range:-180,180'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    user.lat = location.lat
    user.lng = location.lng

    yield user.save()
    response.noContent()
  }

  * destroy (request, response) {
    const user = yield User.findOrFail(request.param('id'))

    if (request.authUser.username !== user.username) {
      response.unauthorized({message: 'Not allowed to delete other users'})
      return
    }

    const userData = request.only('password')
    const validation = yield Validator.validate(userData, {
      password: 'required'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    user.isOwner = true

    try {
      yield request.auth.validate(user.email, userData.password)
    } catch (e) {
      response.unprocessableEntity([
        {
          'field': 'password',
          'validation': 'password_match',
          'message': e.message
        }
      ])
      return
    }

    // Delete all related stuff of the User
    yield Database
      .table('check_ins')
      .where('username', user.username)
      .delete()

    yield Database
      .table('comment_ratings')
      .where('username', user.username)
      .delete()

    yield Database
      .table('comment_ratings')
      .whereIn('comment_id', Database.select('id').from('comments').where('username', user.username))
      .delete()

    yield Database
      .table('comments')
      .where('username', user.username)
      .delete()

    yield Database
      .table('photos')
      .where('username', user.username)
      .update({username: null})

    yield Database
      .table('friendships')
      .where('related_user', user.username).orWhere('relating_user', user.username)
      .delete()

    yield Database
      .table('messages')
      .where('sender', user.username).orWhere('receiver', user.username)
      .delete()

    yield Database
      .table('reset_tokens')
      .where('user', user.username)
      .delete()

    yield Storage.delete(user.profile_picture)

    yield user.delete()
    response.ok({
      message: 'User successfully deleted.'
    })
  }

  /**
   * Generate an email verification token and send it to the user.
   * Besides, this token is set as user property. To persist it,
   * the actual user.save action is still needed.
   * @param user
   */
  * generateEmailConfirmation (user) {
    // Create an email confirmation token
    const token = yield TokenGenerator.make('hex', 16)
    user.confirmation_token = token

    user.isOwner = true

    // Send the confirmation email
    yield Mail.send('emails.confirmation', {
      username: user.username,
      email: user.email,
      confirmLink: Url.resolve(Config.get('app.absoluteUrl'), Route.url('confirm', {token: token}))
    }, (message) => {
      message.to(user.email, user.username)
      message.from('no-reply@iptk.herokuapp.com', 'Sechzehn')
      message.subject('Sechzehn: Verify Your Account')
      message.embed(Path.join(__dirname, '../../../public/assets/logo.png'), 'logo')
    })
  }
}

module.exports = UserController
