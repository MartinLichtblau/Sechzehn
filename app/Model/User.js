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
      // For friends
      'date_of_birth',
      // Only for owner
      'email',
      'incognito',
      'confirmed'
    ]
  }

  static get visibleList () {
    return [
      'username',
      'real_name',
      'city',
      'profile_picture',
      'lat',
      'lng'
    ]
  }

  getLat (lat) {
    if (!this.attributes.incognito || this.attributes.isOwner) {
      return lat
    } else {
      return null
    }
  }

  getLng (lng) {
    if (!this.attributes.incognito || this.attributes.isOwner) {
      return lng
    } else {
      return null
    }
  }

  /**
   * Format the date_of_birth as YYYY-MM-DD and restrict the access to this field.
   *
   * @param dob - the date of birth
   * @returns {*|String}
   */
  getDateOfBirth (dob) {
    if (this.attributes.isOwner || this.attributes.status === 'CONFIRMED') {
      return (dob ? Moment(dob).format('YYYY-MM-DD') : dob)
    } else {
      return null
    }
  }

  getEmail (email) {
    if (this.attributes.isOwner) {
      return email
    } else {
      return null
    }
  }

  getIncognito (val) {
    if (this.attributes.isOwner) {
      return val
    } else {
      return null
    }
  }

  getConfirmed (val) {
    if (this.attributes.isOwner) {
      return val
    } else {
      return null
    }
  }

  static get computed () {
    return ['friendship_status']
  }

  getFriendshipStatus () {
    if (this.status) {
      return this.status
    } else if (this.isOwner) {
      return 'SELF'
    }
    return 'NONE'
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
}

module
  .exports = User
