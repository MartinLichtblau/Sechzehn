'use strict'

const ServiceProvider = require('adonis-fold').ServiceProvider

class StorageProvider extends ServiceProvider {
  * register () {
    this.app.singleton('App/Utils/Storage', function (app) {
      const Storage = require('../app/Utils/Storage')
      return new Storage()
    })
  }
}

module.exports = StorageProvider
