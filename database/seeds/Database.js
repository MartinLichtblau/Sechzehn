'use strict'

/*
 |--------------------------------------------------------------------------
 | Database Seeder
 |--------------------------------------------------------------------------
 | Database Seeder can be used to seed dummy data to your application
 | database. Here you can make use of Factories to create records.
 |
 | make use of Ace to generate a new seed
 |   ./ace make:seed [name]
 |
 */

const Factory = use('Factory')
const User = use('App/Model/User')

class DatabaseSeeder {
  * run () {
    const user = new User()
    user.email = 'a@a.de'
    user.username = 'a'
    user.password = 'a'
    yield user.save()

    yield Factory.model('App/Model/User').create(20)
  }
}

module.exports = DatabaseSeeder
