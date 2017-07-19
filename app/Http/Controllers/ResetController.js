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
const Path = require('path')

class ResetController {
  * requestForm (request, response) {
    yield response.sendView('reset.requestForm')
  }

  * request (request, response) {
    const data = request.only('email')
    const type = request.accepts('json', 'html')

    const validation = yield Validator.validate(data, {email: 'required|email'})

    if (validation.fails()) {
      if (type === 'json') {
        response.unprocessableEntity(validation.messages())
        return
      }
      yield request.with({errors: validation.messages()}).flash()

      response.redirect('back')
      return
    }

    const user = yield User.findBy('email', data.email)

    if (user) {
      const token = yield TokenGenerator.make()

      yield ResetToken.create({
        token: token,
        user: user.username
      })

      yield Mail.send('emails.reset', {
        username: user.username,
        email: user.email,
        resetLink: Url.resolve(Config.get('app.absoluteUrl'), Route.url('reset.confirmForm', {token: token}))
      }, (message) => {
        message.to(user.email, user.username)
        message.from('no-reply@iptk.herokuapp.com', 'Sechzehn')
        message.subject('Sechzehn: Reset Your Password')
        message.embed(Path.join(__dirname, '../../../public/assets/logo.png'), 'logo')
      })
    }

    const message = 'If an user exists for this email address, a reset email was successfully sent. Please check your inbox.'

    if (type === 'json') {
      response.json({message: message})
      return
    }

    yield response.sendView('layouts.simple', {message})
  }

  * confirmForm (request, response) {
    const token = request.param('token')
    yield ResetToken.findOrFail(token)

    const type = request.accepts('json', 'html')

    if (type === 'json') {
      response.json(token)
      return
    }

    yield response.sendView('reset.confirmForm', {token})
  }

  * confirm (request, response) {
    const token = request.param('token')
    const passwords = request.only('password')
    const type = request.accepts('json', 'html')

    const validation = yield Validator.validate(passwords, {password: 'required|string'})

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

    if (type === 'json') {
      response.status(status).json({message: message})
      return
    }

    yield response.status(status).sendView('layouts.simple', {message})
  }
}

module.exports = ResetController
