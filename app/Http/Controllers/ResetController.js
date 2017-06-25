'use strict'

const User = use('App/Model/User')
const ResetToken = use('App/Model/ResetToken')
const TokenGenerator = use('TokenGenerator')
const Validator = use('Validator')
const Mail = use('Mail')
const Route = use('Route')
const Config = use('Config')
const Hash = use('Hash')
const Url = require('url')
const Moment = require('moment')

class ResetController {
  * request (request, response) {
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
        resetLink: Url.resolve(Config.get('app.absoluteUrl'), Route.url('reset.confirmForm', {token: token}))
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

    yield response.sendView('layouts.simple', {message})
  }

  * confirmForm (request, response) {
    const token = request.param('token')
    const resetToken = yield ResetToken.findOrFail(token)

    const type = request.accepts('json', 'html')

    if (type === 'json') {
      response.json(token)
      return
    }

    yield response.sendView('auth.confirmResetForm', {token})
  }

  * confirm (request, response) {
    const token = request.param('token')
    const passwords = request.only('password', 'password_confirmation')
    const type = request.accepts('json', 'html')

    const validation = yield Validator.validate(passwords, {password: 'required|confirmed'})

    if (validation.fails()) {
      if (type === 'json') {
        response.unprocessableEntity(validation.messages())
        return
      }
      yield request.with({errors: validation.messages()}).flash()

      response.redirect('back')
      return
    }

    const resetToken = yield ResetToken.findOrFail(token)

    let message = 'Reset token not valid anymore.'
    let status = 410

    if (Moment().diff(resetToken.created_at, 'days') <= 30) {
      const user = yield resetToken.user().fetch()
      user.password = yield Hash.make(passwords.password)
      yield user.save()

      message = 'Password successfully updated!'
      status = 200
    }

    yield resetToken.delete()

    const type = request.accepts('json', 'html')

    if (type === 'json') {
      response.status(status).json({message: message})
      return
    }

    yield response.status(status).sendView('layouts.simple', {message})
  }

}

module.exports = ResetController
