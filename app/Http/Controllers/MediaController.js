'use strict'

const Helpers = use('Helpers')

class MediaController {
  * show (request, response) {
    try {
      const file = Helpers.storagePath(request.param('filename', ''))
      response.download(file)
    } catch (e) {
      response.notFound().json({'error': 'File Not Found'})
    }
  }
}

module.exports = MediaController
