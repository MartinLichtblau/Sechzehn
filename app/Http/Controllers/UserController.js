'use strict'

const Helpers = use('Helpers')
const Validator = use('Validator')
const Hash = use('Hash')
const Database = use('Database')
const User = use('App/Model/User')
const Route = use('Route')
const Config = use('Config')
const TokenGenerator = use('TokenGenerator')
const Mail = use('Mail')
const Url = require('url')
const Fs = require('fs')
const Path = require('path')
const Exceptions = require('adonis-lucid/src/Exceptions')
const Pagination = require('./Helper/Pagination')

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
    const query = User.query().column(User.visible)

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
        threshold: 0
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
      // query.orderByRaw('distance ASC')

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

    // Fetch the actual data
    const totalResult = yield totalQuery.count().first()
    pagination.data = yield query.forPage(pagination.page, pagination.perPage)
    pagination.total = Number(totalResult.count)
    response.ok(pagination)
  }

  * store (request, response) {
    const userData = request.only('username', 'email', 'password')
    const validation = yield Validator.validate(userData, User.rules)

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const user = yield User.create(userData)

    yield this.generateEmailConfirmation(user)
    yield user.save()

    response.created(user)
  }

  * show (request, response) {
    const authUsername = request.authUser.username

    const user = yield User.query().where('username', request.param('id', null))
      .leftJoin(Database.raw('friendships on users.username = friendships.relating_user and ? = friendships.related_user', [authUsername]))
      .first()

    if (user === null) {
      throw new Exceptions.ModelNotFoundException()
    }

    if (user.username === authUsername) {
      response.ok(user.completeView())
    } else if (user.status === 'CONFIRMED') {
      response.ok(user.friendView())
    } else {
      response.ok(user.strangerView())
    }
  }

  * update (request, response) {
    const user = yield User.findOrFail(request.param('id', null))
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

    response.ok(user.completeView())
  }

  * updateProfilePicture (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

    if (request.authUser.username !== user.username) {
      response.unauthorized({message: 'Not allowed to edit other users'})
      return
    }

    const profilePicture = request.file('profile_picture', {
      maxSize: '500kb',
      allowedExtensions: ['jpg', 'png', 'jpeg', 'JPG', 'PNG', 'JPEG']
    })

    let oldPath = null

    // Save path of the old picture to delete it later
    if (user.profile_picture !== null && user.profile_picture.startsWith(Config.get('app.absoluteUrl'))) {
      oldPath = Helpers.storagePath(user.profile_picture.split('/').pop())
    }

    if (profilePicture === null || profilePicture === '') {
      user.profile_picture = null
      yield user.save()
    } else {
      const fileName = `${new Date().getTime()}.${profilePicture.extension()}`
      yield profilePicture.move(Helpers.storagePath(), fileName)

      if (!profilePicture.moved()) {
        response.badRequest(profilePicture.errors())
        return
      }

      user.profile_picture = Url.resolve(Config.get('app.absoluteUrl'), Route.url('media', {filename: fileName}))
    }

    yield user.save()

    // Delete the old picture
    if (oldPath) {
      Fs.unlink(oldPath, (err) => {
        if (err) console.warn(err)
      })
    }

    response.ok(user.completeView())
  }

  * updatePassword (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

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
      request.auth.validate(user.email, userData.old_password)
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
    response.ok(user.completeView())
  }

  * updateEmail (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

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
    response.ok(user.completeView())
  }

  * updateLocation (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

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
    response.ok(user.completeView())
  }

  * destroy (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

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
