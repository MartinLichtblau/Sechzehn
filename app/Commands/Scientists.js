'use strict'

const Command = use('Command')
const User = use('App/Model/User')

class Scientists extends Command {
  get signature () {
    return 'scientists'
  }

  get description () {
    return 'Add some scientists to the database.'
  }

  * handle (args, options) {
    const demoUsers = [
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
        city: 'Wuerzburg',
        confirmed: true,
        password: 'a',
        profile_picture: 'https://upload.wikimedia.org/wikipedia/commons/f/f8/Bundesarchiv_Bild183-R57262%2C_Werner_Heisenberg.jpg'
      }
    ]

    try {
      const users = yield User.createMany(demoUsers)
      this.info(users)
    } catch (e) {
      this.warn(e)
    }
  }
}

module.exports = Scientists
