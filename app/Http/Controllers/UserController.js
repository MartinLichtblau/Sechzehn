'use strict'

const Validator = use('Validator')
const User = use('App/Model/User')

class UserController {
  * index (request, response) {
    const lat = request.input('lat')
    const lng = request.input('lng')

    const query = User

    if (lat !== null && lng !== null) {
      query.whereRaw()
    }

    const users = yield query.paginate(request.input('page', 1), 10)

    response.ok(users)
  }

  * store (request, response) {
    const userData = request.only('username', 'real_name', 'email', 'password', 'password_confirmation', 'city', 'profile_picture')
    const rules = User.rules()
    const validation = yield Validator.validate(userData, rules)

    if (validation.fails()) {
      response.json(validation.messages())
      return
    }

    const user = yield User.create(userData)

    const token = yield this._generateToken(request, user)

    response.created({
      user,
      token
    })
  }

  * show (request, response) {
    const user = yield User.findBy('id', request.param('id', null))
    response.ok(user)
  }

  * update (request, response) {
    const user = yield User.findBy('id', request.param('id', null))
    const userData = request.only('real_name', 'email', 'password', 'password_confirmation', 'city', 'profile_picture')

    const decodedAuthData = yield request.auth.decode()

    // Check if the authenicated user is the user that should be edited
    if (decodedAuthData.payload.uid !== user.id) {
      response.unauthorized({error: 'Not allowed to edit other users'})
      return
    }

    const rules = User.rulesForUpdate(user.id)
    const validation = yield Validator.validate(userData, rules)

    if (validation.fails()) {
      response.json(validation.messages())
      return
    }

    user.fill(userData)
    const token = yield this._generateToken(request, user)

    response.ok({
      user,
      token
    })
  }

  * destroy (request, response) {
    const user = yield User.findBy('id', request.param('id', null))

    const decodedAuthData = yield request.auth.decode()

    // Check if the authenicated user is the user that should be deleted
    if (decodedAuthData.payload.uid !== user.id) {
      response.unauthorized({error: 'Not allowed to delete other users'})
      return
    }

    yield user.delete()
    response.noContent()
  }

  * login (request, response) {
    const userData = request.all()

    const {
      email,
      password
    } = userData

    try {
      const token = yield request.auth.attempt(email, password)

      response.ok({
        token: token
      })
    } catch (e) {
      response.unauthorized({error: e.message})
    }
  }

  * _generateToken (request, user) {
    return yield request.auth.generate(user)
  }
}

module.exports = UserController
