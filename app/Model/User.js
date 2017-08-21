'use strict'

const Lucid = use('Lucid')
const Hash = use('Hash')
const Moment = require('moment')

class User extends Lucid {
  /**
   * The validation rules for creating a User.
   * @returns {{username: string, email: string, password: string}}
   */
  static get rules () {
    return {
      username: 'required|unique:users',
      email: `required|email|unique:users`,
      password: 'required|string'
    }
  }

  /**
   * The validation rules for updating a User.
   * @param userId
   * @returns {{real_name: string, city: string, date_of_birth: string, incognito: string}}
   */
  static get rulesForUpdate () {
    return {
      real_name: 'string|max:255',
      city: 'string|max:255',
      date_of_birth: 'date|after:1900-01-01|before_offset_of:14,year',
      incognito: 'boolean'
    }
  }

  /**
   * Set the primary key for this Model to username.
   * @returns {string}
   */
  static get primaryKey () {
    return 'username'
  }

  /**
   * The primary key can not be incremented.
   * @returns {boolean}
   */
  static get incrementing () {
    return false
  }

  static boot () {
    super.boot()

    /**
     * Hashing password before storing to the
     * database.
     */
    this.addHook('beforeCreate', function* (next) {
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
   * The fields which are visible per default for this Model (i.e for JSON serialization).
   *
   * @returns {[string,string,string,string,string,string,string,string,string]}
   */
  static get visible () {
    return [
      'username',
      'real_name',
      'city',
      'profile_picture',
      'lat',
      'lng',
      'lol'
    ]
  }

  getLat () {
    console.log(this)
    console.log(this.lol)

    if (!this.incognito || this.lol) {
      return this.lat
    } else {
      return null
    }
  }

  /**
   * Builds an object with all relevant properties of this user for viewing the profile.
   * @returns {{username: (string|string), real_name: (*|string), city: (*|string), profile_picture: (*|profile_picture|{fileExtension}|null), lat: (*|lat|{min, max}|string|string), lng: (*|string|lng|{min, max}), friendship_status: string}}
   */
  strangerView () {
    return {
      username: this.username,
      real_name: this.real_name,
      city: this.city,
      profile_picture: this.profile_picture,
      lat: this.lat,
      lng: this.lng,
      friendship_status: this.status || 'NONE'
    }
  }

  /**
   * Builds an object with all relevant properties of this user for viewing the own profile.
   * @returns {{id: number, username: string, email: string, real_name: (string|null), date_of_birth: (Date|null), city: (string|null), profile_picture: (string|null), lat: (null|float), lng: (null|float), incognito: boolean, confirmed: boolean}}
   */
  completeView () {
    return {
      username: this.username,
      email: this.email,
      real_name: this.real_name,
      date_of_birth: this.date_of_birth,
      city: this.city,
      profile_picture: this.profile_picture,
      lat: this.lat,
      lng: this.lng,
      incognito: this.incognito,
      confirmed: this.confirmed,
      friendship_status: 'SELF',
      checkins: this.checkins
    }
  }

  /**
   * Builds an object with all relevant properties of this user for viewing the profile as a friend.
   * @returns {{username: (string|string), email: (string|string), real_name: (*|string), date_of_birth: (string|*), city: (*|string), profile_picture: (*|profile_picture|{fileExtension}|null), lat: (*|string|lat|{min, max}|string), lng: (*|lng|{min, max}|string), friendship_status}}
   */
  friendView () {
    return {
      username: this.username,
      real_name: this.real_name,
      date_of_birth: this.date_of_birth,
      city: this.city,
      profile_picture: this.profile_picture,
      lat: this.lat,
      lng: this.lng,
      friendship_status: this.status
    }
  }

  /**
   * List all ResetTokens for this User.
   * @returns {Object}
   */
  resetTokens () {
    return this.hasMany('App/Model/ResetToken', 'username', 'user')
  }

  /**
   * List all Friends and requested Friends of this User.
   * @returns {Object}
   */
  friends () {
    return this.belongsToMany('App/Model/User', 'friendships', 'relating_user', 'related_user')
  }

  /**
   * List all Check-Ins of the this User.
   * @returns {Object}
   */
  checkins () {
    return this.hasMany('App/Model/CheckIn', 'username', 'username')
  }

  /**
   * Format the date_of_birth as YYYY-MM-DD.
   *
   * @param dob - the date of birth
   * @returns {*|String}
   */
  getDateOfBirth (dob) {
    return (dob ? Moment(dob).format('YYYY-MM-DD') : dob)
  }
}

module.exports = User
