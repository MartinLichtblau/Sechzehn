'use strict'

const Message = use('App/Model/Message')
const User = use('App/Model/User')
const Validator = use('Validator')

class MessageController {
  constructor (socket, request, presence) {
    this.socket = socket
    this.request = request
    this.presence = presence

    presence.track(socket, socket.currentUser.username, {})

    console.log('Connected %s', socket.id)
  }

  * onMessage (messageData) {
    const sender = this.socket.currentUser
    const receiver = yield User.find(messageData.receiver)

    if (receiver === null) {
      this.emitError('Receiver Not Found')
      return
    }

    const validation = yield Validator.validate(messageData, Message.rules)

    if (validation.fails()) {
      this.socket.toMe().emit('error', validation.messages())
      return
    }

    const message = yield Message.create({
      sender: sender.username,
      receiver: receiver.username,
      body: messageData.body
    })

    const receiverSockets = this.getUserSockets(receiver.username)
    const senderSockets = this.getUserSockets(sender.username)

    this.socket.to(receiverSockets.concat(senderSockets)).emit('message', message)
  }

  * onRead (messageData) {
    const me = this.socket.currentUser.username
    const other = messageData.sender

    const message = yield Message.find(messageData.id)

    if (message === null || message.receiver !== me || message.sender !== other) {
      this.emitError('Message Not Found')
      return
    }

    message.is_read = message.is_read || messageData.is_read
    yield message.save()

    const receiverSockets = this.getUserSockets(message.receiver)
    const senderSockets = this.getUserSockets(message.sender)

    this.socket.to(receiverSockets.concat(senderSockets)).emit('read', message)
  }

  disconnected (socket) {
    console.log('Disconnected %s', socket.id)
  }

  getUserSockets (username) {
    const sockets = this.presence.get(username)
    return sockets ? sockets.map((user) => user.socket.id) : []
  }

  emitError (message) {
    this.socket.toMe().emit('error', {
      message: message
    })
  }
}

module.exports = MessageController
