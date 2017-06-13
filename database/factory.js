'use strict'

const knex = require('knex')({
  dialect: 'postgres'
})

const gis = require('knex-postgis')(knex)

/*
 |--------------------------------------------------------------------------
 | Model and Database Factory
 |--------------------------------------------------------------------------
 |
 | Factories let you define blueprints for your database models or tables.
 | These blueprints can be used with seeds to create fake entries. Also
 | factories are helpful when writing tests.
 |
 */

const Factory = use('Factory')

/*
 |--------------------------------------------------------------------------
 | User Model Blueprint
 |--------------------------------------------------------------------------
 | Below is an example of blueprint for User Model. You can make use of
 | this blueprint inside your seeds to generate dummy data.
 |
 */
Factory.blueprint('App/Model/User', (fake) => {
  return {
    username: fake.username(),
    email: fake.email(),
    password: fake.password(),
    real_name: fake.name(),
    date_of_birth: fake.date(),
    city: fake.city(),
    profile_picture: fake.avatar({fileExtension: 'jpg'}),
    location: gis.point(fake.longitude({min: 7, max: 10}), fake.latitude({min: 49, max: 51}))
  }
}
)
