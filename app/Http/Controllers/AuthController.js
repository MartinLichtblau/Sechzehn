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
}

module.exports = AuthController
