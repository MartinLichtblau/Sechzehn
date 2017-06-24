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

Route.on('/').render('welcome')

Route.get('/media/:filename', 'MediaController.show').as('media')

Route.get('confirm/:token', 'AuthController.confirmEmail').as('confirm')
Route.get('reset', 'AuthController.requestResetForm').as('requestReset')
Route.get('reset/:token', 'AuthController.confirmResetForm').as('confirmReset')

Route.group('auth', function () {
  Route.post('login', 'AuthController.login')
  Route.post('reset', 'AuthController.requestReset')
  Route.post('reset/:token', 'AuthController.confirmReset')
}).prefix('/api/auth')
  .formats(['json'], false)

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
