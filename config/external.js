'use strict'

const Env = use('Env')

module.exports = {
  /*
   |--------------------------------------------------------------------------
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
