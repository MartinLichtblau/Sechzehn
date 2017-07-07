'use strict'

const Message = use('App/Model/Message')
const User = use('App/Model/User')
const Validator = use('Validator')

class MessageController {
  * index (request, response) {
    const username = request.authUser.username
    const chats = yield Message.query().where('sender', username).orWhere('receiver', username)

    response.ok(chats)
  }

  * store (request, response) {
    const sender = request.authUser
    const receiver = yield User.findOrFail(request.param('id', null))
    const messageData = request.only('body')

    const validation = yield Validator.validate(messageData, Message.rules)

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const message = yield Message.create({
      sender: sender.username,
      receiver: receiver.username,
      body: messageData.body
    })

    response.ok(message)
  }

  * show (request, response) {
    const me = request.authUser.username
    const other = request.param('id', null)

    const page = Number(request.input('page', 1))
    const perPage = Number(request.input('per_page', 10))

    const validation = yield Validator.validate({page, perPage}, {
      page: 'integer|min:1',
      perPage: 'integer|min:1'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const messages = yield Message.query().where(function () {
      this.where('sender', me)
        .orWhere('receiver', me)
    }).where(function () {
      this.where('sender', other)
        .orWhere('receiver', other)
    }).orderBy('created_at', 'desc').paginate(page, perPage)

    response.ok(messages)
  }

  * update (request, response) {
    //
  }

  * destroy (request, response) {
    //
  }
}

module.exports = MessageController
