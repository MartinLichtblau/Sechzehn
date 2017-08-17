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
  },

  /*
  |----------------------------------------------------------
  | Cloudinary
  |--------------------------------------------------------------------------
  |
  | Here we define api credentials for Cloudinary.
  |
  */
  cloudinary: {
    url: Env.get('CLOUDINARY_URL')
  }
}
