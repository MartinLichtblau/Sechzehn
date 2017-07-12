'use strict'

const Lucid = use('Lucid')

class Message extends Lucid {
  /**
   * The validation rules for creating a Message.
   */
  static get rules () {
    return {
      body: 'required|string'
    }
  }
}

module.exports = Message
