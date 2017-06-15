'use strict'

class AuthController {
  * login (request, response) {
    const credentials = request.only('email', 'password')

    console.log(credentials)

    const {
      email,
      password
    } = credentials

    try {
      const token = yield request.auth.attempt(email, password)

      response.ok({
        token: token
      })
    } catch (e) {
      response.unauthorized({error: e.message})
    }
  }
}

module.exports = AuthController
