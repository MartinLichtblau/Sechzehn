'use strict'

const Lucid = use('Lucid')

class Comment extends Lucid {
  static get hidden () {
    return [
      'updated_at',
      'venue_id',
      'username',
      'photo_id'
    ]
  }

  venue () {
    return this.belongsTo('App/Model/Venue')
  }

  user () {
    return this.belongsTo('App/Model/User', 'username', 'username')
  }

  photo () {
    return this.belongsTo('App/Model/Photo')
  }

  commentRatingsPositive () {
    return this.hasMany('App/Model/CommentRating').where('thumbs_up', true)
  }

  commentRatingsNegative () {
    return this.hasMany('App/Model/CommentRating').where('thumbs_up', false)
  }
}

module.exports = Comment
