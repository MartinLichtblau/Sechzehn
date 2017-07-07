'use strict'

const Lucid = use('Lucid')
const Database = use('Database')

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
