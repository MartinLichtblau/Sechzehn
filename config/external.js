'use strict'

const Env = use('Env')

module.exports = {
  /*
   |--------------------------------------------------------------------------
   | Foursquare
   |--------------------------------------------------------------------------
   |
   | Here we define api options for Foursquare.
   |
   */
  foursquare: {
    id: Env.get('FOURSQUARE_CLIENT_ID'),
    secret: Env.get('FOURSQUARE_CLIENT_SECRET')
  }
}
