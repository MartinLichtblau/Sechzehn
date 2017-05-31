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

Route.group('v1', function () {
  Route
    .resource('users', 'UserController')
    .middleware({
      auth: ['update', 'destroy']
    })
}).prefix('/api/v1')
  .formats(['json'], false)
