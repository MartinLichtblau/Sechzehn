'use strict'

const ServiceProvider = require('adonis-fold').ServiceProvider

class StorageProvider extends ServiceProvider {
  * register () {
    this.app.singleton('Adonis/Custom/Storage', function (app) {
      const Storage = require('./Storage')
      return new Storage()
    })
  }
}

module.exports = StorageProvider
