'use strict'

const Lucid = use('Lucid')
const Database = use('Database')

class Comment extends Lucid {
  static get hidden () {
    return [
      'updated_at',
      'venue_id',
      'username',
      'photo_id',
      'comment_id'
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

  static get ratingQuery () {
    return Database.select('comment_id', Database.raw('sum(rating) as rating')).from('comment_ratings').groupBy('comment_id').as('rating_query')
  }
}

module.exports = Comment
