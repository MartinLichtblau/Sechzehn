'use strict'

const ServiceProvider = require('adonis-fold').ServiceProvider

class TokenGeneratorProvider extends ServiceProvider {
  * register () {
    this.app.singleton('Adonis/Custom/TokenGenerator', function (app) {
      const TokenGenerator = require('./TokenGenerator')
      return new TokenGenerator()
    })
  }
}

module.exports = TokenGeneratorProvider
