'use strict'

const Lucid = use('Lucid')
const Hash = use('Hash')

class User extends Lucid {
  /**
   * The validation rules for creating a User.
   * @returns {{username: string, email: string, password: string}}
   */
  static rules () {
    return {
      username: 'required|unique:users',
      email: `required|email|unique:users`,
      password: 'required|string'
    }
  }

  /**
   * The validation rules for updating a User.
   * @param userId
   * @returns {{username: string, real_name: string, city: string, date_of_birth: string, incognito: string}}
   */
  static rulesForUpdate (userId) {
    return {
      username: `required|unique:users,username,id,${userId}`,
      real_name: 'string|max:255',
      city: 'string|max:255',
      date_of_birth: 'date|after:1900-01-01|before_offset_of:14,year',
      incognito: 'boolean'
    }
  }

  /**
   * The fields which are visible per default for this Model (i.e for JSON serialization).
   *
   * @returns {[string,string,string,string,string,string,string,string,string]}
   */
  static get visible () {
    return [
      'id',
      'username',
      'real_name',
      'city',
      'profile_picture',
      'date_of_birth',
      'lat',
      'lng'
    ]
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

  /**
   * A query scope to filter all Users that are not in incognito mode.
   * @param builder the query builder
   */
  static scopeUnhidden (builder) {
    builder.whereNot('incognito', true)
  }

  /**
   * Builds an object with all relevant properties of this user for the complete profile view.
   * @returns {{id: number, username: string, email: string, real_name: (string|null), date_of_birth: (Date|null), city: (string|null), profile_picture: (string|null), lat: (null|float), lng: (null|float), incognito: boolean, confirmed: boolean}}
   */
  complete () {
    return {
      id: this.id,
      username: this.username,
      email: this.email,
      real_name: this.real_name,
      date_of_birth: this.date_of_birth,
      city: this.city,
      profile_picture: this.profile_picture,
      lat: this.lat,
      lng: this.lng,
      incognito: this.incognito,
      confirmed: this.confirmed
    }
  }

  /**
   * List all ResetTokens for this User.
   * @returns {Object}
   */
  resetTokens () {
    return this.belongsToMany('App/Model/ResetToken')
  }
}

module.exports = User
