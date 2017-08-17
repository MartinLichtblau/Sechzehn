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
Route.on('/chat').render('chat')

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
  Route.patch('users/:id/profile_picture', 'UserController.updateProfilePicture').middleware('auth')
  Route.patch('users/:id/password', 'UserController.updatePassword').middleware('auth')
  Route.patch('users/:id/email', 'UserController.updateEmail').middleware('auth')
  Route.patch('users/:id/location', 'UserController.updateLocation').middleware('auth')

  Route.get('users/:id/friends', 'FriendshipController.index').middleware('auth')
  Route.get('users/:id/friends/requests', 'FriendshipController.requests').middleware('auth')
  Route.post('users/:id/friends', 'FriendshipController.store').middleware('auth')
  Route.patch('users/:id/friends', 'FriendshipController.update').middleware('auth')
  Route.delete('users/:id/friends', 'FriendshipController.destroy').middleware('auth')

  Route.get('messages', 'MessageController.index').middleware('auth')
  Route.get('messages/:id', 'MessageController.show').middleware('auth')
  Route.post('messages/:id', 'MessageController.store').middleware('auth')
  Route.patch('messages/:id/:message', 'MessageController.update').middleware('auth')

  Route
    .resource('venues', 'VenueController')
    .only('index', 'show')
    .middleware({auth: ['show']})

  Route
    .resource('users', 'UserController')
    .except('create', 'edit')
    .middleware({auth: ['index', 'show', 'update', 'destroy']})
}).prefix('/api')
  .formats(['json'], false)
