'use strict'

const Helpers = use('Helpers')
const Validator = use('Validator')
const Hash = use('Hash')
const Database = use('Database')
const User = use('App/Model/User')
const Route = use('Route')
const Config = use('Config')
const Url = require('url')
const Fs = require('fs')

class UserController {
  * index (request, response) {
    const lat = request.input('lat')
    const lng = request.input('lng')
    const radius = request.input('radius', 10)
    const page = Number(request.input('page', 1))
    const perPage = Number(request.input('per_page', 10))

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
    const userData = request.only('username', 'email', 'password', 'password_confirmation')
    const validation = yield Validator.validate(userData, User.rules())

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    // Remove password_confirmation from userData object because this value should and could not be persisted
    delete userData.password_confirmation

    const user = yield User.create(userData)

    const token = yield request.auth.generate(user)

    response.created({
      user,
      token
    })
  }

  * show (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))

    response.ok(user)
  }

  /**
   * Let the user get his own profile with all information.
   * @param request
   * @param response
   */
  * showComplete (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))

    if (request.authUser.id !== user.id) {
      response.unauthorized({error: 'Not allowed to show all information of other users'})
      return
    }

    response.ok(user.complete())
  }

  * update (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))
    const userData = request.only('username', 'real_name', 'city', 'date_of_birth', 'incognito')

    if (request.authUser.id !== user.id) {
      response.unauthorized({error: 'Not allowed to edit other users'})
      return
    }

    const rules = User.rulesForUpdate(user.id)
    const validation = yield Validator.validate(userData, rules)

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    user.fill(userData)
    yield user.save()

    response.ok(user.complete())
  }

  * updateProfilePicture (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))

    if (request.authUser.id !== user.id) {
      response.unauthorized({error: 'Not allowed to edit other users'})
      return
    }

    const profilePicture = request.file('profile_picture', {
      maxSize: '1mb',
      allowedExtensions: ['jpg', 'png', 'jpeg']
    })

    if (profilePicture === null || profilePicture === '') {
      user.profile_picture = null
      yield user.save()
      response.ok(user)
      return
    }

    const fileName = `${new Date().getTime()}.${profilePicture.extension()}`
    yield profilePicture.move(Helpers.storagePath(), fileName)

    if (!profilePicture.moved()) {
      response.badRequest(profilePicture.errors())
      return
    }

    // Delete the old picture
    if (user.profile_picture !== null && user.profile_picture.startsWith(Config.get('app.absoluteUrl'))) {
      const oldPath = Helpers.storagePath(user.profile_picture.split('/').pop())

      Fs.unlink(oldPath, (err) => {
        if (err) console.warn(err)
      })
    }

    user.profile_picture = Url.resolve(Config.get('app.absoluteUrl'), Route.url('media', {filename: fileName}))
    yield user.save()
    response.ok(user.complete())
  }

  * updatePassword (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))

    if (request.authUser.id !== user.id) {
      response.unauthorized({error: 'Not allowed to edit other users'})
      return
    }

    const userData = request.only('old_password', 'password', 'password_confirmation')
    const validation = yield Validator.validate(userData, {password: 'required|confirmed', old_password: 'required'})

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

    user.password = Hash.make(userData.password)
    yield user.save()
    response.ok(user.complete())
  }

  * updateEmail (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))

    if (request.authUser.id !== user.id) {
      response.unauthorized({error: 'Not allowed to edit other users'})
      return
    }

    const userData = request.only('password', 'email')
    const validation = yield Validator.validate(userData, {
      password: 'required',
      email: `required|email|unique:users,email,id,${user.id}`
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    try {
      request.auth.validate(user.email, userData.password)
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

    user.email = userData.email
    yield user.save()
    response.ok(user.complete())
  }

  *
  destroy (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))

    if (request.authUser.id !== user.id) {
      response.unauthorized({error: 'Not allowed to delete other users'})
      return
    }

    yield user.delete()
    response.noContent()
  }
}

module.exports = UserController
