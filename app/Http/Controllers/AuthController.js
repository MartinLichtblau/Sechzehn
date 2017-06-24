'use strict'

const User = use('App/Model/User')
const ResetToken = use('App/Model/ResetToken')
const TokenGenerator = use('TokenGenerator')
const Validator = use('Validator')
const Mail = use('Mail')
const Route = use('Route')
const Config = use('Config')
const Url = require('url')

class AuthController {
  * login (request, response) {
    const credentials = request.only('email', 'password')

    const validation = yield Validator.validate(credentials, {
      email: 'required|email',
      password: 'required'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
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
      response.unauthorized({error: e.message})
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

    yield response.sendView('simpleMessage', {message})
  }

  * requestReset (request, response) {
    const data = request.only('email')

    const validation = yield Validator.validate(data, {email: 'required|email'})

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const user = yield User.findBy('email', data.email)

    if (user) {
      const token = yield TokenGenerator.make()

      yield ResetToken.create({
        token: token,
        user_id: user.id
      })

      yield Mail.send('emails.reset', {
        username: user.username,
        email: user.email,
        resetLink: Url.resolve(Config.get('app.absoluteUrl'), Route.url('confirmReset', {token: token}))
      }, (message) => {
        message.to(user.email, user.username)
        message.from('sechzehn@tw-co.de')
        message.subject('Sechzehn: Reset Your Password')
      })
    }

    const message = 'If an user exists for this email address, a reset email was successfully sent. Please check your inbox.'

    const type = request.accepts('json', 'html')

    if (type === 'json') {
      response.json({message: message})
      return
    }

    yield response.sendView('simpleMessage', {message})
  }

  * confirmResetForm (request, response) {
    const token = request.param('token')
  }

  * confirmReset (request, response) {
    const token = request.param('token')
    const passwords = request.only('password', 'password_confirmation')

    const validation = yield Validator.validate(passwords, {password: 'required|confirmed'})

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const resetToken = yield ResetToken.findOrFail(token)
    const user = yield resetToken.user().fetch()

    response.ok(user)
  }
}

module.exports = AuthController
