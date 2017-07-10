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

    console.log('Received message from %s to %s with body: %s', sender.username, messageData.receiver, messageData.body)

    if (receiver === null) {
      this.socket.toMe().emit('error', {
        message: 'Receiver Not Found'
      })
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

  disconnected (socket) {
    console.log('Disconnected %s', socket.id)
  }

  getUserSockets (username) {
    const sockets = this.presence.get(username)
    return sockets ? sockets.map((user) => user.socket.id) : []
  }
}

module.exports = MessageController
