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
      email,
      password
    } = userData

    const user = yield User.create({
      username,
      email,
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

    const rules = User.rules(user.id)
    const validation = yield Validator.validate(userData, rules)

    if (validation.fails()) {
      response.json(validation.messages())
      return
    }

    const {
      username,
      email,
      password
    } = userData

    user.fill({
      username,
      email,
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
    response.status(204).send()
  }

  * _generateToken (request, user) {
    return yield request.auth.generate(user)
  }
}

module.exports = UserController
