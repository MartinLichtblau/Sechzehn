'use strict'

const User = use('App/Model/User')
const Validator = use('Validator')

class AuthController {
  * login (request, response) {
    const credentials = request.only('email', 'password')

    const validation = yield Validator.validate(credentials, {
      email: 'required|email',
      password: 'required'
    })

    if (validation.fails()) {
      response.unauthorized({error: 'Email or password wrong'})
      return
    }

    try {
      const token = yield request.auth.attempt(credentials.email, credentials.password)
      const user = yield User.findBy('email', credentials.email)

      response.ok({
        user: user,
        token: token
      })
    } catch (e) {
      response.unauthorized({error: 'Email or password wrong'})
    }
  }

  * confirmEmail (request, response) {
    const token = request.param('token')
    const user = yield User.findByOrFail('confirmation_token', token)

    user.confirmed = true
    user.confirmation_token = null
    yield user.save()

    const message = 'Email confirmed'

    const type = request.accepts('json', 'html')

    if (type === 'json') {
      response.json({message: message})
      return
    }

    yield response.sendView('layouts.simple', {message})
  }
}

module.exports = AuthController
