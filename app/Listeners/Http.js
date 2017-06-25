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
  error.message = 'Something bad happened!'

  if (error.name === 'ModelNotFoundException') {
    error.status = 404
    error.message = 'Resource Not Found'
    manualHandled = true
  }

  if (error.name === 'PasswordMisMatch') {
    error.status = 400
    error.message = 'Invalid Credentials'
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
  console.error(error.stack)

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
