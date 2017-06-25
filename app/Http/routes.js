'use strict'

/*
 |--------------------------------------------------------------------------
 | Router
 |--------------------------------------------------------------------------
 |
 | AdonisJs Router helps you in defining urls and their actions. It supports
 | all major HTTP conventions to keep your routes file descriptive and
 | clean.
 |
 | @example
 | Route.get('/user', 'UserController.index')
 | Route.post('/user', 'UserController.store')
 | Route.resource('user', 'UserController')
 */

const Route = use('Route')

/**
 * Homepage
 */
Route.on('/').render('welcome')

/**
 * Media handling
 */
Route.get('/media/:filename', 'MediaController.show').as('media')

/**
 * Login and email verification
 */
Route.group('auth', function () {
  Route.post('login', 'AuthController.login')
  Route.get('confirm/:token', 'AuthController.confirmEmail').as('confirm')
})

/**
 * Password reset
 */
Route.group('reset', function () {
  Route.get('reset', 'ResetController.requestForm')
  Route.post('reset', 'ResetController.request')
  Route.get('reset/:token', 'ResetController.confirmForm').as('reset.confirmForm')
  Route.post('reset/:token', 'ResetController.confirm')
})

Route.group('api', function () {
  Route.get('users/:id/complete', 'UserController.showComplete').middleware('auth')
  Route.patch('users/:id/profile_picture', 'UserController.updateProfilePicture').middleware('auth')
  Route.patch('users/:id/password', 'UserController.updatePassword').middleware('auth')
  Route.patch('users/:id/email', 'UserController.updateEmail').middleware('auth')

  Route
    .resource('users', 'UserController')
    .except('create', 'edit')
    .middleware({
      auth: ['update', 'destroy']
    })
}).prefix('/api')
  .formats(['json'], false)
