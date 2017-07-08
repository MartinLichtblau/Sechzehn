'use strict'

const Message = use('App/Model/Message')
const User = use('App/Model/User')
const Validator = use('Validator')
const Database = use('Database')
const Exceptions = require('adonis-lucid/src/Exceptions')

class MessageController {
  * index (request, response) {
    const username = request.authUser.username

    const receiverQuery = Database.select('sender', 'receiver', 'body', 'created_at', 'receiver as username')
      .from('messages').where('sender', username)

    const senderQuery = Database.select('sender', 'receiver', 'body', 'created_at', 'sender as username')
      .from('messages').where('receiver', username)

    const chatData = yield Database
      .distinct(
        Database.raw(
          `chats.username, users.real_name, users.profile_picture,
          first_value(sender) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) as last_sender,
          first_value(receiver) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) last_receiver,
          first_value(chats.created_at) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) as last_created_at,
          first_value(body) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) as last_body`
        ))
      .select()
      .from(receiverQuery.union(senderQuery).as('chats')).innerJoin('users', 'chats.username', 'users.username')
      .orderBy('last_created_at', 'desc')

    const chats = chatData.map(function (row) {
      return {
        user: {
          username: row.username,
          real_name: row.real_name,
          profile_picture: row.profile_picture
        },
        last_message: {
          sender: row.last_sender,
          receiver: row.last_receiver,
          body: row.last_body,
          created_at: row.last_created_at
        }
      }
    })

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
    const me = request.authUser.username
    const other = request.param('id', null)
    const data = request.all()

    const message = yield Message.findOrFail(request.param('message', null))

    if (message.receiver === me && message.sender === other) {
      message.is_read = message.is_read || Validator.sanitizor.toBoolean(data.is_read)

      yield message.save()
    } else if (message.sender === me && message.receiver === other) {
      response.forbidden({
        message: 'You are not allowed to modify the read status for others.'
      })
    } else {
      throw new Exceptions.ModelNotFoundException()
    }

    response.ok(message)
  }
}

module.exports = MessageController
