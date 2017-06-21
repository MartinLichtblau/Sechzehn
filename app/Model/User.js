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
      city: 'string|max:255'
    }
  }

  /**
   * The fields which are visible per default for this Model.
   *
   * @returns {[string,string,string,string,string]}
   */
  static get visible () {
    return [
      'id',
      'username',
      'real_name',
      'city',
      'profile_picture',
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
}

module.exports = User
