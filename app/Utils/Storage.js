'use strict'

const Helpers = use('Helpers')
const Config = use('Config')
const Route = use('Route')
const Fs = require('fs')
const Url = require('url')
const NE = require('node-exceptions')
const Cloudinary = require('cloudinary')

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
  /**
   * Upload an image to Cloudinary if available otherwise to the local storage.
   *
   * @param fileWrapper - instance of the AdonisJs file class
   * @returns {String|null} the url
   */
  * store (fileWrapper) {
    if (fileWrapper === null || fileWrapper === '') {
      return null
    } else {
      if (Config.get('external.cloudinary.url')) {
        // Upload to Cloudinary
        const uploadedFile = yield Cloudinary.v2.uploader.upload(fileWrapper.file.path, {
          width: 1500,
          height: 1500,
          crop: 'limit'
        }, result => {
          return result
        })

        return uploadedFile.secure_url
      }

      const fileName = `${new Date().getTime()}.${fileWrapper.extension()}`

      yield fileWrapper.move(Helpers.storagePath(), fileName)

      if (!fileWrapper.moved()) {
        throw StorageException.raise(fileWrapper.errors())
      }

      return Url.resolve(Config.get('app.absoluteUrl'), Route.url('media', {filename: fileName}))
    }
  }

  /**
   * Delete the file for the given url if possible.
   *
   * @param url - the url to the file.
   */
  * delete (url) {
    if (url === null) {
      return
    }

    // If the url points to the application storage folder
    if (url.startsWith(Config.get('app.absoluteUrl'))) {
      const oldPath = Helpers.storagePath(url.split('/').pop())
      Fs.unlink(oldPath, (err) => {
        if (err) console.warn(err)
      })
    }

    // If the url points to Cloudinary
    if (Config.get('external.cloudinary.url') && url.startsWith('https://res.cloudinary.com')) {
      Cloudinary.v2.uploader.destroy(url.split('/').pop().split('.')[0], (err) => {
        if (err) console.warn(err)
      })
    }
  }
}

module.exports = Storage
