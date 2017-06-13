'use strict'

const Lucid = use('Lucid')
const Hash = use('Hash')

const Wkx = require('wkx')

class User extends Lucid {
  static rules (userId) {
    return {
      username: 'required|unique:users',
      email: `required|email|unique:users,email,id,${userId}`,
      password: 'required|confirmed'
    }
  }

  /**
   * The fields which are visible per default for this Model.
   *
   * @returns {[string,string,string,string,string,string]}
   */
  static get visible () {
    return ['username', 'real_name', 'city', 'profile_picture', 'location']
  }

  static boot () {
    super.boot()

    /**
     * Hashing password before storing to the
     * database.
     */
    this.addHook('beforeCreate', function * (next) {
      this.password = yield Hash.make(this.password)
      yield next
    })
  }

  apiTokens () {
    return this.hasMany('App/Model/Token')
  }

  /**
   * Transform the buffered WKT representation used by PostGIS into a GeoJson Point object with longitude and latitude.
   * @param location
   */
  getLocation (location) {
    if (typeof location !== 'undefined' && location !== null) {
      const wkbBuffer = Buffer.from(location, 'hex')
      return Wkx.Geometry.parse(wkbBuffer).toGeoJSON()
    } else return ''
  }
}

module.exports = User
