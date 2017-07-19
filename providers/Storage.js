'use strict'

const Helpers = use('Helpers')
const Config = use('Config')
const Route = use('Route')
const Fs = require('fs')
const Url = require('url')
const NE = require('node-exceptions')

class StorageException extends NE.RuntimeException {
  /**
   * default error code to be used for raising
   * exceptions
   *
   * @return {Number}
   */
  static get defaultErrorCode () {
    return 500
  }

  /**
   * This exception is raised when an error occurs during storage operations.
   *
   * @param  {String} message
   * @return {Object}
   */
  static raise (message) {
    return new this(message, this.defaultErrorCode)
  }
}

class Storage {
  * store (file) {
    if (file === null || file === '') {
      return null
    } else {
      const fileName = `${new Date().getTime()}.${file.extension()}`
      yield file.move(Helpers.storagePath(), fileName)

      if (!file.moved()) {
        throw StorageException.raise(file.errors())
      }

      return Url.resolve(Config.get('app.absoluteUrl'), Route.url('media', {filename: fileName}))
    }
  }

  * delete (url) {
    // If the url points to the application storage folder
    if (url !== null && url.startsWith(Config.get('app.absoluteUrl'))) {
      const oldPath = Helpers.storagePath(url.split('/').pop())
      Fs.unlink(oldPath, (err) => {
        if (err) console.warn(err)
      })
    }
  }
}

module.exports = Storage
