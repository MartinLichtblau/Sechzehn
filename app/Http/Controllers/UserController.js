'use strict'

const Helpers = use('Helpers')
const Validator = use('Validator')
const User = use('App/Model/User')
const Route = use('Route')
const Config = use('Config')
const Url = require('url')

class UserController {
  * index (request, response) {
    const lat = request.input('lat')
    const lng = request.input('lng')
    const radius = request.input('radius', 10)

    const query = User.query()

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

      query.whereRaw('earth_box(ll_to_earth(?, ?), ?) @> ll_to_earth(lat, lng)', [lat, lng, radius * 1000])
    }

    const users = yield query.paginate(request.input('page', 1), request.input('per_page', 10))

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

  * update (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))
    const userData = request.only('username', 'real_name', 'city', 'date_of_birth')

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

    response.ok(user)
  }

  * destroy (request, response) {
    const user = yield User.findOrFail(Number(request.param('id', null)))

    if (request.authUser.id !== user.id) {
      response.unauthorized({error: 'Not allowed to delete other users'})
      return
    }

    yield user.delete()
    response.noContent()
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

    const fileName = `${new Date().getTime()}.${profilePicture.extension()}`
    yield profilePicture.move(Helpers.storagePath(), fileName)

    if (!profilePicture.moved()) {
      response.badRequest(profilePicture.errors())
      return
    }

    user.profile_picture = Url.resolve(Config.get('app.absoluteUrl'), Route.url('media', {filename: fileName}))
    yield user.save()
    response.ok(user)
  }
}

module.exports = UserController
