'use strict'

const User = use('App/Model/User')

class AuthController {
  * login (request, response) {
    const credentials = request.only('email', 'password')

    try {
      const token = yield request.auth.attempt(credentials.email, credentials.password)
      const user = yield User.findBy('email', credentials.email)

      response.ok({
        user: user,
        token: token
      })
    } catch (e) {
      response.unauthorized({error: e.message})
    }
  }

  * confirmEmail (request, response) {
    const token = request.param('token')
    const type = request.accepts('json', 'html')
    const user = yield User.findByOrFail('confirmation_token', token)

    user.confirmed = true
    user.confirmation_token = null
    yield user.save()

    const message = 'Email confirmed'

    if (type === 'json') {
      response.json({message: message})
      return
    }

    yield response.sendView('simpleMessage', {message})
  }

  * requestReset (request, response) {
    const email = request.param('email')
  }

  * confirmReset (request, response) {
    const token = request.param('token')
    const passwords = request.only('password', 'password_confirmation')
  }
}

module.exports = AuthController
