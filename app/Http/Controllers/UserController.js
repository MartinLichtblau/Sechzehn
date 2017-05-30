'use strict'

class UserController {

  * index (request, response) {
    const users = yield User.all() // fetch users
    response.json(users)
  }

  * create (request, response) {
    //
  }

  * store (request, response) {
    //
  }

  * show (request, response) {
    //
  }

  * edit (request, response) {
    //
  }

  * update (request, response) {
    //
  }

  * destroy (request, response) {
    //
  }

}

module.exports = UserController
