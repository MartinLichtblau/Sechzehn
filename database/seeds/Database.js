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

// const Factory = use('Factory')
const User = use('App/Model/User')

class DatabaseSeeder {
  * run () {

    const demoUsers = [
      {
        username: 'a',
        real_name: 'Chuck Norris',
        email: 'a@a.de',
        date_of_birth: '1940-03-10',
        lat: '49.873486',
        lng: '8.628927',
        city: 'Ryan, Oklahoma',
        confirmed: true,
        password: 'a',
        profile_picture: 'https://assets.cdn.moviepilot.de/files/9d036b0839d190dd2983610c9ade7b3caa40f6d94909c0bcf3ff8764b295/500/500/chuck.jpg'
      },
      {
        username: 'Nille',
        real_name: 'Niels Bohr',
        email: 'niels@bohr.dk',
        date_of_birth: '1885-10-07',
        lat: '49.873486',
        lng: '8.628927',
        city: 'Kopenhagen',
        confirmed: true,
        password: 'a',
        profile_picture: 'https://upload.wikimedia.org/wikipedia/commons/6/6d/Niels_Bohr.jpg'
      },
      {
        username: 'Radioactive',
        real_name: 'Marie Curie',
        email: 'marie@curie.pl',
        date_of_birth: '1867-11-07',
        lat: '49.861175',
        lng: '8.681241',
        city: 'Warschau',
        confirmed: true,
        password: 'a',
        profile_picture: 'https://upload.wikimedia.org/wikipedia/commons/7/71/Marie_Curie_c1920.png'
      },
      {
        username: 'Ada2711',
        real_name: 'Ada Lovelace',
        email: 'ada@lovelace.co.uk',
        date_of_birth: '1852-11-27',
        lat: '49.872492',
        lng: '8.670948',
        city: 'London',
        confirmed: true,
        password: 'a',
        profile_picture: 'https://upload.wikimedia.org/wikipedia/commons/8/87/Ada_Lovelace.jpg'
      },
      {
        username: 'BlueCrystal',
        real_name: 'Werner Heisenberg',
        email: 'werner@heisenberg.de',
        date_of_birth: '1901-12-05',
        lat: '49.891018',
        lng: '8.666669',
        city: 'Würzburg',
        confirmed: true,
        password: 'a',
        profile_picture: 'https://upload.wikimedia.org/wikipedia/commons/f/f8/Bundesarchiv_Bild183-R57262%2C_Werner_Heisenberg.jpg'
      }
    ]

    try {
      yield User.createMany(demoUsers)
    } catch (e) {
      console.warn(e)
    }

    // yield Factory.model('App/Model/User').create(20)
  }
}

module.exports = DatabaseSeeder
