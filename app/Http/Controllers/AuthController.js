'use strict'

class AuthController {
  * login (request, response) {
    const credentials = request.only('email', 'password')

    try {
      const token = yield request.auth.attempt(credentials.email, credentials.password)

      response.ok({
        token: token
      })
    } catch (e) {
      response.unauthorized({error: e.message})
    }
  }
}

module.exports = AuthController
