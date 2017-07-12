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
      this.warn('Receiver Not Found', sender.username)
      return
    }

    if (sender.username === receiver.username) {
      this.warn('You can not write messages to yourself.', sender.username)
      return
    }

    const validation = yield Validator.validate(messageData, Message.rules)

    const senderSockets = this.getUserSockets(sender.username)

    if (validation.fails()) {
      yield this.socket.to(senderSockets).emit('warning', validation.messages())
      return
    }

    const message = yield Message.create({
      sender: sender.username,
      receiver: receiver.username,
      body: messageData.body
    })

    const receiverSockets = this.getUserSockets(receiver.username)

    this.socket.to(receiverSockets.concat(senderSockets)).emit('message', message)
  }

  * onRead (messageData) {
    const me = this.socket.currentUser.username
    const other = messageData.sender

    const message = yield Message.find(messageData.id)

    if (message === null) {
      this.warn('Message Not Found', me)
      return
    }

    if (message.sender === me) {
      this.warn('You can not set the read status of your own messages', me)
      return
    }

    if (message.receiver !== me || message.sender !== other) {
      this.warn('Message Not Found', me)
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

  warn (message, toUsername) {
    const sockets = this.getUserSockets(toUsername)
    this.socket.to(sockets).emit('warning', {
      message: message
    })
  }
}

module.exports = MessageController
