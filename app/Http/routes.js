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

Route.group('auth', function () {
  Route.post('login', 'AuthController.login')
}).prefix('/api/auth')
  .formats(['json'], false)

Route.group('api', function () {
  Route
    .resource('users', 'UserController')
    .except('create', 'edit')
    .middleware({
      auth: ['update', 'destroy']
    })
}).prefix('/api')
  .formats(['json'], false)
