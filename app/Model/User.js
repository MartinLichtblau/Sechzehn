'use strict'

const Lucid = use('Lucid')
const Hash = use('Hash')

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
    return ['username', 'real_name', 'city', 'profile_picture', 'last_lat', 'last_lng']
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
}

module.exports = User
