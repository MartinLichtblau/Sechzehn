'use strict'

const Validator = use('Validator')
const User = use('App/Model/User')
const Hash = use('Hash')

class UserController {
  * index (request, response) {
    const users = yield User.all() // fetch users
    response.ok(users)
  }

  * store (request, response) {
    const userData = request.all()
    const rules = User.rules(-1)
    const validation = yield Validator.validate(userData, rules)

    if (validation.fails()) {
      response.json(validation.messages())
      return
    }

    const {
      username,
      real_name,
      email,
      password,
      city
    } = userData

    const user = yield User.create({
      username,
      real_name,
      email,
      city,
      password: yield Hash.make(password)
    })

    const token = yield this._generateToken(request, user)

    response.status(201).send({
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
    const userData = request.all()

    const decodedAuthData = yield request.auth.decode()

    // Check if the authenicated user is the user that should be edited
    if (decodedAuthData.payload.uid !== user.id) {
      response.unauthorized({error: 'Not allowed to edit other users'})
      return
    }

    const rules = User.rules(user.id)
    const validation = yield Validator.validate(userData, rules)

    if (validation.fails()) {
      response.json(validation.messages())
      return
    }

    const {
      username,
      real_name,
      email,
      password,
      city
    } = userData

    user.fill({
      username,
      real_name,
      email,
      city,
      password: yield Hash.make(password)
    })
    const token = yield this._generateToken(request, user)

    response.status(201).send({
      user,
      token
    })
  }

  * destroy (request, response) {
    const user = yield User.findBy('id', request.param('id', null))
    yield user.delete()
    response.noContent().send()
  }

  * _generateToken (request, user) {
    return yield request.auth.generate(user)
  }
}

module.exports = UserController
