'use strict'

const ServiceProvider = require('adonis-fold').ServiceProvider

class TokenGeneratorProvider extends ServiceProvider {
  * register () {
    this.app.singleton('App/Utils/TokenGenerator', function (app) {
      const TokenGenerator = require('../app/Utils/TokenGenerator')
      return new TokenGenerator()
    })
  }
}

module.exports = TokenGeneratorProvider
