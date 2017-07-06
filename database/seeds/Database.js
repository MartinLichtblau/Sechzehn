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
    const userA = new User()
    userA.email = 'a@a.de'
    userA.username = 'a'
    userA.password = 'a'
    userA.confirmed = true
    yield userA.save()

    const userB = new User()
    userB.email = 'b@a.de'
    userB.username = 'b'
    userB.password = 'a'
    userB.confirmed = true
    yield userB.save()

    yield Factory.model('App/Model/User').create(20)
  }
}

module.exports = DatabaseSeeder
