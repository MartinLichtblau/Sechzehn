'use strict'

const Lucid = use('Lucid')
const Hash = use('Hash')

class User extends Lucid {
  static rules () {
    return {
      username: 'required|unique:users',
      email: `required|email|unique:users`,
      password: 'required|confirmed'
    }
  }

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
   * date format to be used for setting dates inside the table.
   * dates will be manipulated with moment.
   *
   * @return {String}
   *
   * @public
   */
  static get dateFormat () {
    return 'YYYY-MM-DD'
  }

  /**
   * The fields which are visible per default for this Model.
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

    /**
     * Hashing password before updating the
     * database.
     */
    this.addHook('beforeUpdate', function * (next) {
      this.password = yield Hash.make(this.password)
      yield next
    })
  }

  /**
   * Format the date of birth before its returned (i.e. in JSON).
   * @param date
   * @returns {String}
   */
  getDateOfBirth (date) {
    return this.formatDate(date)
  }

  static scopeUnhidden (builder) {
    builder.whereNot('incognito', true)
  }

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
      incognito: this.incognito
    }
  }
}

module.exports = User
