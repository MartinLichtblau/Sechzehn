'use strict'

const crypto = require('crypto')

class TokenGenerator {
  /**
   * Generate a random token using the crypto library.
   *
   * @param stringBase the base for the bytes.toString method (default: hex)
   * @param byteLength the byte length (default: 16)
   * @returns {Promise}
   */
  make (stringBase, byteLength) {
    const base = stringBase || 'hex'
    const length = byteLength || 16
    return new Promise((resolve, reject) => {
      crypto.randomBytes(length, (err, buffer) => {
        if (err) {
          reject(err)
        } else {
          resolve(buffer.toString(base))
        }
      })
    })
  }
}

module.exports = TokenGenerator
