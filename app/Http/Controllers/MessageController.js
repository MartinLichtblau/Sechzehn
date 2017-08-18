'use strict'

const Message = use('App/Model/Message')
const User = use('App/Model/User')
const Validator = use('Validator')
const Database = use('Database')
const Exceptions = require('adonis-lucid/src/Exceptions')
const Pagination = require('./Helper/Pagination')
const Moment = require('moment')

class MessageController {
  * index (request, response) {
    const username = request.authUser.username

    const pagination = new Pagination(request)

    const receiverQuery = Database.select('sender', 'receiver', 'body', 'created_at', 'updated_at', 'is_read', 'receiver as username')
      .from('messages').where('sender', username)

    const senderQuery = Database.select('sender', 'receiver', 'body', 'created_at', 'updated_at', 'is_read', 'sender as username')
      .from('messages').where('receiver', username)

    const chatData = yield Database
      .distinct(
        Database.raw(
          `chats.username, users.real_name, users.profile_picture,
          first_value(chats.sender) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) as last_sender,
          first_value(chats.receiver) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) last_receiver,
          first_value(chats.created_at) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) as last_created_at,
          first_value(chats.updated_at) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) last_updated_at,
          first_value(chats.is_read) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) as last_is_read,
          first_value(chats.body) OVER (PARTITION BY chats.username ORDER BY chats.created_at DESC) as last_body`
        ))
      .select()
      .from(receiverQuery.union(senderQuery).as('chats')).innerJoin('users', 'chats.username', 'users.username')
      .orderBy('last_created_at', 'desc').forPage(pagination.page, pagination.perPage)

    // Total count needed for manually creating the pagination
    const totalQuery = yield Database.from(receiverQuery.union(senderQuery)).countDistinct('username').first()
    pagination.total = Number(totalQuery.count)

    // Transform the data
    pagination.data = chatData.map(row => {
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
          created_at: this.formatDate(row.last_created_at),
          updated_at: this.formatDate(row.last_updated_at),
          is_read: row.last_is_read
        }
      }
    })

    response.ok(pagination)
  }

  * store (request, response) {
    const sender = request.authUser
    const receiver = yield User.findOrFail(request.param('id'))
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
    const other = request.param('id')

    let page = Number(request.input('page', 1))
    let perPage = Number(request.input('per_page', 10))

    if (isNaN(page) || page < 1) {
      page = 1
    }

    if (isNaN(perPage) || perPage < 1) {
      perPage = 10
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
    const other = request.param('id')
    const data = request.all()

    const message = yield Message.findOrFail(request.param('message'))

    if (message.receiver === me && message.sender === other) {
      message.is_read = message.is_read === true || Validator.sanitizor.toBoolean(data.is_read)

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

  /**
   * Format the the as YYYY-MM-DD HH:mm:ss.
   *
   * @param date - the date
   * @returns {*|String}
   */
  formatDate (date) {
    return (date ? Moment(date).format('YYYY-MM-DD HH:mm:ss') : date)
  }
}

module.exports = MessageController
