'use strict'

const Env = use('Env')
const Youch = use('youch')
const Http = exports = module.exports = {}

/**
 * handle errors occured during a Http request.
 *
 * @param  {Object} error
 * @param  {Object} request
 * @param  {Object} response
 */
Http.handleError = function * (error, request, response) {
  error.status = error.status || 500
  const type = request.accepts('json', 'html')
  // Define if the error is a special case that is handled below
  let manualHandled = false

  // Map some exceptions to HTTP status codes with corresponding message
  switch (error.name) {
    case 'ModelNotFoundException':
      error.status = 404
      error.message = 'Resource Not Found'
      manualHandled = true
      break
    case 'HttpException':
      error.status = 404
      error.message = 'Route Not Found'
      manualHandled = true
      break
    case 'PasswordMisMatch':
      error.status = 400
      error.message = 'Invalid Credentials'
      manualHandled = true
      break
    case 'InvalidLoginException':
      error.status = 401
      error.message = 'Unauthorized'
      manualHandled = true
  }

  /**
   * DEVELOPMENT REPORTER
   */
  if (!manualHandled && Env.get('NODE_ENV') === 'development') {
    const youch = new Youch(error, request.request)
    const formatMethod = type === 'json' ? 'toJSON' : 'toHTML'
    const formattedErrors = yield youch[formatMethod]()
    response.status(error.status).send(formattedErrors)
    return
  }

  /**
   * PRODUCTION REPORTER
   */
  console.error(error)

  if (!manualHandled) error.message = 'Something bad happened!'

  if (type === 'json') {
    response.status(error.status).json({error: error.message})
    return
  }

  yield response.status(error.status).sendView('error', {error})
}

/**
 * listener for Http.start event, emitted after
 * starting http server.
 */
Http.onStart = function () {
}
