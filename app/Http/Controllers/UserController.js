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

class UserController {
  * index (request, response) {
    const lat = request.input('lat')
    const lng = request.input('lng')
    const radius = request.input('radius', 10)
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

      const query = User.query().unhidden().column(User.visible)
      // Calculate the distance between the search point and the user's position
        .select(Database.raw('(earth_distance(ll_to_earth(lat, lng), ll_to_earth(?, ?)) / 1000) as distance', [lat, lng]))

      // Check if location of the user is in the circle around the search point
      // See https://www.postgresql.org/docs/8.3/static/earthdistance.html
      const inRadiusQuery = 'earth_box(ll_to_earth(?, ?), ?) @> ll_to_earth(lat, lng)'
      query.whereRaw(inRadiusQuery, [lat, lng, radius * 1000])
      // query.orderByRaw('distance ASC')
      const users = yield query.forPage(page, perPage)

      // Total count needed for manually creating the pagination
      const totalQuery = yield User.query().unhidden().whereRaw(inRadiusQuery, [lat, lng, radius * 1000]).count().first()
      const total = Number(totalQuery.count)

      response.ok({
        total: Number(total),
        perPage: perPage,
        currentPage: page,
        lastPage: Math.ceil(total / perPage),
        data: users
      })
      return
    }

    const users = yield User.query().unhidden().paginate(page, perPage)
    response.ok(users)
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
      response.ok(user.complete())
    } else if (user.status === 'CONFIRMED') {
      response.ok(user.friend())
    } else {
      response.ok(user.stranger())
    }
  }

  * update (request, response) {
    const user = yield User.findOrFail(request.param('id', null))
    const userData = request.only('real_name', 'city', 'date_of_birth', 'incognito')

    if (request.authUser.username !== user.username) {
      response.unauthorized({error: 'Not allowed to edit other users'})
      return
    }

    const validation = yield Validator.validate(userData, User.rulesForUpdate)

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    user.fill(userData)
    yield user.save()

    response.ok(user.complete())
  }

  * updateProfilePicture (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

    if (request.authUser.username !== user.username) {
      response.unauthorized({error: 'Not allowed to edit other users'})
      return
    }

    const profilePicture = request.file('profile_picture', {
      maxSize: '500kb',
      allowedExtensions: ['jpg', 'png', 'jpeg']
    })

    // Delete the old picture
    if (user.profile_picture !== null && user.profile_picture.startsWith(Config.get('app.absoluteUrl'))) {
      const oldPath = Helpers.storagePath(user.profile_picture.split('/').pop())

      Fs.unlink(oldPath, (err) => {
        if (err) console.warn(err)
      })
    }

    if (profilePicture === null || profilePicture === '') {
      user.profile_picture = null
      yield user.save()
      response.ok(user.complete())
      return
    }

    const fileName = `${new Date().getTime()}.${profilePicture.extension()}`
    yield profilePicture.move(Helpers.storagePath(), fileName)

    if (!profilePicture.moved()) {
      response.badRequest(profilePicture.errors())
      return
    }

    user.profile_picture = Url.resolve(Config.get('app.absoluteUrl'), Route.url('media', {filename: fileName}))
    yield user.save()
    response.ok(user.complete())
  }

  * updatePassword (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

    if (request.authUser.username !== user.username) {
      response.unauthorized({error: 'Not allowed to edit other users'})
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
    response.ok(user.complete())
  }

  * updateEmail (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

    if (request.authUser.username !== user.username) {
      response.unauthorized({error: 'Not allowed to edit other users'})
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
    response.ok(user.complete())
  }

  * updateLocation (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

    if (request.authUser.username !== user.username) {
      response.unauthorized({error: 'Not allowed to edit other users'})
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
    response.ok(user.complete())
  }

  * destroy (request, response) {
    const user = yield User.findOrFail(request.param('id', null))

    if (request.authUser.username !== user.username) {
      response.unauthorized({error: 'Not allowed to delete other users'})
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
